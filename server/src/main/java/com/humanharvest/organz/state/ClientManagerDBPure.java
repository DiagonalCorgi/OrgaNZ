package com.humanharvest.organz.state;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.TransplantRequest;
import com.humanharvest.organz.database.DBManager;
import com.humanharvest.organz.utilities.enums.*;
import com.humanharvest.organz.views.client.PaginatedClientList;
import com.humanharvest.organz.utilities.type_converters.EnumSetToString;
import com.humanharvest.organz.views.client.PaginatedDonatedOrgansList;
import com.humanharvest.organz.views.client.PaginatedTransplantList;
import org.hibernate.ReplicationMode;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A pure database implementation of {@link ClientManager} that uses a database to store clients, then retrieves them
 * every time a request is made (no caching).
 */
public class ClientManagerDBPure implements ClientManager {

    private final DBManager dbManager;

    public ClientManagerDBPure() {
        this.dbManager = DBManager.getInstance();
    }

    public ClientManagerDBPure(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public List<Client> getClients() {
        List<Client> clients = null;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            clients = dbManager.getDBSession()
                    .createQuery("FROM Client", Client.class)
                    .getResultList();
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return clients == null ? new ArrayList<>() : clients;
    }

    public PaginatedClientList getClients(
            String q,
            Integer offset,
            Integer count,
            Integer minimumAge,
            Integer maximumAge,
            Set<String> regions,
            EnumSet<Gender> birthGenders,
            ClientType clientType,
            EnumSet<Organ> donating,
            EnumSet<Organ> requesting,
            ClientSortOptionsEnum sortOption,
            Boolean isReversed) {

        Transaction trns = null;

        String isDonor = "EXISTS (SELECT donating.Client_uid FROM Client_organsDonating AS donating WHERE donating.Client_uid=c.uid LIMIT 1)";
        String notIsDonor = "NOT EXISTS (SELECT donating.Client_uid FROM Client_organsDonating AS donating WHERE donating.Client_uid=c.uid LIMIT 1)";
        String isRequesting = "EXISTS (SELECT requesting.Client_uid FROM TransplantRequest AS requesting WHERE requesting.Client_uid=c.uid LIMIT 1)";
        String notIsRequesting = "NOT EXISTS (SELECT requesting.Client_uid FROM TransplantRequest AS requesting WHERE requesting.Client_uid=c.uid LIMIT 1)";

        //TODO: Make this use the complex sort as in ClientNameSorter
        String nameSort = "lastName";

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();


            Map<String, Object> params = new HashMap<>();
            StringBuilder joinBuilder = new StringBuilder();
            StringJoiner whereJoiner = new StringJoiner(" AND ");


            //Setup minimum age filter
            if (minimumAge != null) {
                //Use the TIMESTAMPDIFF (MySQL only) function to calculate age
                whereJoiner.add("TIMESTAMPDIFF(YEAR, c.dateOfBirth, NOW()) >= :minimumAge");
                params.put("minimumAge", minimumAge);
            }

            //Setup maximum age filter
            if (maximumAge != null) {
                //Use the TIMESTAMPDIFF (MySQL only) function to calculate age
                whereJoiner.add("TIMESTAMPDIFF(YEAR, c.dateOfBirth, NOW()) <= :maximumAge");
                params.put("maximumAge", maximumAge);
            }

            //Setup region filter. We use region IN, then have to do some fancy string conversions as the Hibernate params weren't working
            if (regions != null && regions.size() > 0) {
                //TODO: Work out why the params didn't work with EnumSet or even when converting it to string so we can use that instead of this uglyness
                whereJoiner.add("c.region IN (" + regions.stream().map(region -> "'" + region.replace("'", "''") + "'").collect(Collectors.joining(",")) + ")");
//                params.put("regions", regions);
            }

            //Setup birth gender filter. We use gender IN, then have to do some fancy string conversions as the Hibernate params weren't working
            if (birthGenders != null && birthGenders.size() > 0) {
                //TODO: Work out why the params didn't work with EnumSet or even when converting it to string so we can use that instead of this uglyness
                whereJoiner.add("c.gender IN (" + birthGenders.stream().map(birthGender -> "'" + birthGender.name().replace("'", "''") + "'").collect(Collectors.joining(",")) + ")");
//                params.put("genders", birthGenders);
            }

            //Setup donating filter. We use an INNER JOIN and therefor select only clients where they have an entry in the Client_organsDonating table that matches one of the given organs
            if (donating != null && donating.size() > 0) {
                //TODO: Work out why the params didn't work with EnumSet or even when converting it to string so we can use that instead of this uglyness
                String joinQuery = " INNER JOIN (SELECT donating.Client_uid FROM Client_organsDonating AS donating WHERE donating.organsDonating IN (";

                joinQuery += donating.stream().map(organ -> "'" + organ.name().replace("'", "''") + "'").collect(Collectors.joining(",")) + ")";

                joinQuery += " GROUP BY donating.Client_uid) donating ON c.uid=donating.Client_uid ";

                joinBuilder.append(joinQuery);
//                params.put("donating", donating);
            }

            //Setup requesting filter. We use an INNER JOIN and therefor select only clients where they have an entry in the TransplantRequest table that matches one of the given organs and is status=WAITING
            if (requesting != null && requesting.size() > 0) {
                //TODO: Work out why the params didn't work with EnumSet or even when converting it to string so we can use that instead of this uglyness
                String joinQuery = " INNER JOIN (SELECT requesting.Client_uid FROM TransplantRequest AS requesting WHERE" +
                        " requesting.status='WAITING' AND " +
                        " requesting.requestedOrgan IN (";

                joinQuery += requesting.stream().map(organ -> "'" + organ.ordinal() + "'").collect(Collectors.joining(",")) + ")";

                joinQuery += " GROUP BY requesting.Client_uid) requesting ON c.uid=requesting.Client_uid ";

                joinBuilder.append(joinQuery);
//                params.put("donating", donating);
            }

            //Setup the client type filter. For this we use an EXISTS (or NOT) then a separate SELECT on the respective table where uid=uid.
            //LIMIT 1 is an efficiency increase as we do not need to keep looking once we have a result (boolean true)
            if (clientType != null) {
                switch (clientType) {
                    case BOTH:
                        whereJoiner.add(isDonor);
                        whereJoiner.add(isRequesting);
                        break;

                    case NEITHER:
                        whereJoiner.add(notIsDonor);
                        whereJoiner.add(notIsRequesting);
                        break;

                    case ONLY_DONOR:
                        whereJoiner.add(isDonor);
                        whereJoiner.add(notIsRequesting);
                        break;

                    case ONLY_RECEIVER:
                        whereJoiner.add(notIsDonor);
                        whereJoiner.add(isRequesting);
                        break;
                }
            }

            //Setup the name filter. For this we make a series of OR checks on the names, if any is true it's true.
            //Checks any portion of any name
            if (q != null && q.length() > 0) {
                StringJoiner qOrJoiner = new StringJoiner(" OR ");
                qOrJoiner.add("UPPER(c.firstName) LIKE UPPER(:q)");
                qOrJoiner.add("UPPER(c.middleName) LIKE UPPER(:q)");
                qOrJoiner.add("UPPER(c.preferredName) LIKE UPPER(:q)");
                qOrJoiner.add("UPPER(c.lastName) LIKE UPPER(:q)");
                whereJoiner.add("(" + qOrJoiner.toString() + ")");
                params.put("q", "%" + q + "%");
            }

            //Set offset to zero if not given
            if (offset == null || offset < 0) {
                offset = 0;
            }
            //Set count to just 30 if not given
            if (count == null || count < 0) {
                //TODO: Should this be 30 or max if not specified
                count = 30;
            }

            //Setup the sort order for the given sort option. Default to NAME if none is given
            if (sortOption == null) {
                sortOption = ClientSortOptionsEnum.NAME;
            }
            String sort;
            String dir;

            switch (sortOption) {
                case ID:
                    sort = "uid";
                    break;
                case AGE:
                    //TODO: Also make this part handle DOD for age
                    sort = "dateOfBirth";
                    break;
                case DONOR:
                    sort = isDonor;
                    break;
                case RECEIVER:
                    sort = isRequesting;
                    break;
                case REGION:
                    sort = "region";
                    break;
                case BIRTH_GENDER:
                    sort = "gender";
                    break;
                case NAME:
                default:
                    sort = nameSort;
            }
            if (isReversed != null && isReversed) {
                dir = "DESC";
            } else {
                dir = "ASC";
            }


            // Create the final strings, in the basic format
            // START_TEXT + JOINS + WHERES + ORDER BY + LIMIT + OFFSET
            // Only add the WHERE if there are some where checks.
            // We also do a second query for count using count(*) and no LIMIT, OFFSET

            if (whereJoiner.length() != 0) {
                joinBuilder.append(" WHERE ");
            }

            // Quite a complex string build, but all defined as above, just simple string combinations
            String queryString = "SELECT c.* FROM Client c " + joinBuilder + whereJoiner.toString() + " ORDER BY " + sort + " " + dir + ", " + nameSort + " ASC LIMIT :limit OFFSET :offset";
            String countString = "SELECT count(*) FROM Client c " + joinBuilder + whereJoiner.toString();

            System.out.println(queryString);

            Query countQuery = session.createNativeQuery(countString);
            Query<Client> mainQuery = session.createNativeQuery(queryString, Client.class);

            // Go through the params and set the values.
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                mainQuery.setParameter(entry.getKey(), entry.getValue());
                countQuery.setParameter(entry.getKey(), entry.getValue());
            }


            // Set the limit and offset for the main query
            mainQuery.setParameter("limit", count);
            mainQuery.setParameter("offset", offset);


            // Execute the queries
            int totalCount = Integer.valueOf(countQuery.uniqueResult().toString());
            List<Client> clients = mainQuery.getResultList();

            return new PaginatedClientList(clients, totalCount);

        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
            return null;
        }
        /*
        return clients == null ? new ArrayList<>() : clients;

        //Setup the primarySorter for the given sort option. Default to NAME if none is given
        if (sortOption == null) {
            sortOption = ClientSortOptionsEnum.NAME;
        }
        Comparator<Client> primarySorter;
        switch (sortOption) {
            case ID:
                primarySorter = Comparator.comparing(Client::getUid, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case AGE:
                primarySorter = Comparator.comparing(Client::getAge, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case DONOR:
                primarySorter = Comparator.comparing(Client::isDonor, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case RECEIVER:
                primarySorter = Comparator
                        .comparing(Client::isReceiver, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case REGION:
                primarySorter = Comparator
                        .comparing(Client::getRegion, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case BIRTH_GENDER:
                primarySorter = Comparator
                        .comparing(Client::getGender, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case NAME:
            default:
                primarySorter = new ClientNameSorter(q);
        }

        //Setup a second comparison
        Comparator<Client> dualSorter = primarySorter.thenComparing(new ClientNameSorter(q));

        //If the sort should be reversed
        if (isReversed != null && isReversed) {
            dualSorter = dualSorter.reversed();
        }

        List<Client> filteredClients = stream
                .filter(q == null ? c -> true : client -> client.nameContains(q))

                .filter(minimumAge == null ? c -> true : client -> client.getAge() >= minimumAge)

                .filter(maximumAge == null ? c -> true : client -> client.getAge() <= maximumAge)

                .filter(regions == null ? c -> true : client -> regions.isEmpty() ||
                        regions.contains(client.getRegion()))

                .filter(birthGenders == null ? c -> true : client -> birthGenders.isEmpty() ||
                        birthGenders.contains(client.getGender()))

                .filter(clientType == null ? c -> true : client -> client.isOfType(clientType))

                .filter(donating == null ? c -> true : client -> donating.isEmpty() ||
                        donating.stream().anyMatch(organ -> client.getCurrentlyDonatedOrgans().contains(organ)))

                .filter(requesting == null ? c -> true : client -> requesting.isEmpty() ||
                        requesting.stream().anyMatch(organ -> client.getCurrentlyRequestedOrgans().contains(organ)))

                .collect(Collectors.toList());


        return new PaginatedClientList(paginatedClients, totalResults);
        */
    }

    @Override
    public void setClients(Collection<Client> clients) {
        Transaction trns = null;
        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();

            session.createQuery("DELETE FROM Client").executeUpdate();

            for (Client client : clients) {
                session.save(client);
            }

            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }
    }

    @Override
    public void addClient(Client client) {
        dbManager.saveEntity(client);
    }

    @Override
    public void removeClient(Client client) {
        Transaction trns = null;
        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();

            dbManager.getDBSession().remove(client);

            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }
    }

    @Override
    public void applyChangesTo(Client client) {
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();

            try {
                dbManager.getDBSession().update(client);
                trns.commit();
            } catch (OptimisticLockException exc) {
                // TODO fix this hack
                try (org.hibernate.Session otherSession = dbManager.getDBSession()) {
                    trns = otherSession.beginTransaction();
                    dbManager.getDBSession().replicate(client, ReplicationMode.OVERWRITE);
                    trns.commit();
                }
            }

        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }
    }

    @Override
    public Optional<Client> getClientByID(int id) {
        Transaction trns = null;
        Client client = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();

            client = dbManager.getDBSession().find(Client.class, id);

            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return Optional.ofNullable(client);
    }

    @Override
    public boolean doesClientExist(String firstName, String lastName, LocalDate dateOfBirth) {
        boolean collision = false;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            collision = dbManager.getDBSession().createQuery("SELECT c FROM Client c "
                    + "WHERE c.firstName = :firstName "
                    + "AND c.lastName = :lastName "
                    + "AND c.dateOfBirth = :dateOfBirth", Client.class)
                    .setParameter("firstName", firstName)
                    .setParameter("lastName", lastName)
                    .setParameter("dateOfBirth", dateOfBirth)
                    .getResultList().size() > 0;
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return collision;
    }

    @Override
    public Collection<TransplantRequest> getAllTransplantRequests() {
        List<TransplantRequest> requests = null;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            requests = dbManager.getDBSession()
                    .createQuery("FROM TransplantRequest", TransplantRequest.class)
                    .getResultList();
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return requests == null ? new ArrayList<>() : requests;
    }

    @Override
    public Collection<TransplantRequest> getAllCurrentTransplantRequests() {
        List<TransplantRequest> requests = null;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            requests = dbManager.getDBSession()
                    .createQuery("SELECT req FROM TransplantRequest req "
                                    + "WHERE req.status = "
                                    + "com.humanharvest.organz.utilities.enums.TransplantRequestStatus.WAITING",
                            TransplantRequest.class)
                    .getResultList();
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return requests == null ? new ArrayList<>() : requests;
    }

    @Override
    public PaginatedTransplantList getAllCurrentTransplantRequests(Integer offset, Integer count,
                                                                   Set<Region> regions, Set<Organ> organs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<HistoryItem> getAllHistoryItems() {
        List<HistoryItem> requests = null;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            requests = dbManager.getDBSession()
                    .createQuery("SELECT item FROM HistoryItem item", HistoryItem.class)
                    .getResultList();
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return requests == null ? new ArrayList<>() : requests;
    }

    /**
     * @return a list of all organs available for donation
     */
    @Override
    public Collection<DonatedOrgan> getAllOrgansToDonate() {
        List<DonatedOrgan> requests = null;
        Transaction trns = null;

        try (org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            requests = dbManager.getDBSession()
                    .createQuery("FROM DonatedOrgan", DonatedOrgan.class)
                    .getResultList();
            System.out.println(requests);
            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }

        return requests == null ? new ArrayList<>() : requests;
    }

    /**
     * @return a list of all organs available for donation
     */
    @Override
    public PaginatedDonatedOrgansList getAllOrgansToDonate(Set<String> regions, EnumSet<Organ> organType) {
        //TODO Implement the WHERE statements for this.
        List<DonatedOrgan> requests = null;
        Transaction trns = null;

        throw new NotImplementedException();

//        return requests == null ? new ArrayList<>() : requests;
    }

    @Override
    public DonatedOrgan manuallyExpireOrgan(DonatedOrgan organ){
        //Todo: Test
        Transaction trns = null;
        try(org.hibernate.Session session = dbManager.getDBSession()) {
            trns = session.beginTransaction();
            dbManager.getDBSession().remove(organ);

            trns.commit();
        } catch (RollbackException exc) {
            if (trns != null) {
                trns.rollback();
            }
        }
            return organ;

        }

}
