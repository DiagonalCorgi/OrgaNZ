package com.humanharvest.organz.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javafx.stage.Stage;

import com.humanharvest.organz.Administrator;
import com.humanharvest.organz.Client;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.resolvers.CommandRunner;
import com.humanharvest.organz.resolvers.CommandRunnerRest;
import com.humanharvest.organz.resolvers.actions.ActionResolver;
import com.humanharvest.organz.resolvers.actions.ActionResolverMemory;
import com.humanharvest.organz.resolvers.actions.ActionResolverRest;
import com.humanharvest.organz.resolvers.administrator.AdministratorResolver;
import com.humanharvest.organz.resolvers.administrator.AdministratorResolverMemory;
import com.humanharvest.organz.resolvers.administrator.AdministratorResolverRest;
import com.humanharvest.organz.resolvers.administrator.FileResolver;
import com.humanharvest.organz.resolvers.administrator.FileResolverMemory;
import com.humanharvest.organz.resolvers.administrator.FileResolverRest;
import com.humanharvest.organz.resolvers.client.ClientResolver;
import com.humanharvest.organz.resolvers.client.ClientResolverMemory;
import com.humanharvest.organz.resolvers.client.ClientResolverRest;
import com.humanharvest.organz.resolvers.clinician.ClincianResolverMemory;
import com.humanharvest.organz.resolvers.clinician.ClinicianResolver;
import com.humanharvest.organz.resolvers.clinician.ClinicianResolverRest;
import com.humanharvest.organz.resolvers.config.ConfigResolver;
import com.humanharvest.organz.resolvers.config.ConfigResolverMemory;
import com.humanharvest.organz.resolvers.config.ConfigResolverRest;
import com.humanharvest.organz.utilities.RestErrorHandler;
import com.humanharvest.organz.utilities.enums.Country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * A static class to store the current state of the system.
 */
public final class State {

    public enum DataStorageType {
        MEMORY, REST
    }

    public enum UiType {
        STANDARD, TOUCH
    }

    private static String baseUri = "http://csse-s302g7.canterbury.ac.nz:8080/";
    private static DataStorageType currentStorageType = DataStorageType.MEMORY;

    private static ClientManager clientManager;
    private static ClientResolver clientResolver;
    private static ClinicianManager clinicianManager;
    private static AdministratorManager administratorManager;
    private static AuthenticationManager authenticationManager;
    private static ImageManager imageManager;
    private static CommandRunner commandRunner;
    private static ActionResolver actionResolver;
    private static ClinicianResolver clinicianResolver;
    private static AdministratorResolver administratorResolver;
    private static FileResolver fileResolver;
    private static ConfigManager configManager;
    private static ConfigResolver configResolver;

    private static Session session;
    private static boolean unsavedChanges;
    private static List<MainController> mainControllers = new ArrayList<>();
    private static RestTemplate restTemplate = new RestTemplate();
    private static String clientEtag = "";
    private static String clinicianEtag = "";
    private static String administratorEtag = "";
    private static String recentEtag = "";
    private static String token = "";
    private static Clinician viewedClinician;
    private static Set<Country> allowedCountries;
    private static UiType uiType = UiType.STANDARD;
    private static Stage primaryStage;
    private static Client spiderwebDonor;
    private static boolean useHackyMouseTouch;

    private State() {
    }

    /**
     * Initialises a new action invoker, client manager and clinician manager.
     */
    public static void init(DataStorageType storageType) {
        currentStorageType = storageType;

        if (storageType == DataStorageType.REST) {
            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            restTemplate.setRequestFactory(requestFactory);
            restTemplate.setErrorHandler(new RestErrorHandler());
            restTemplate.getMessageConverters().removeIf(o ->
                    o instanceof MappingJackson2HttpMessageConverter);
            restTemplate.getMessageConverters().add(customConverter());

            clientManager = new ClientManagerRest();
            clientResolver = new ClientResolverRest();
            clinicianManager = new ClinicianManagerRest();
            administratorManager = new AdministratorManagerRest();
            authenticationManager = new AuthenticationManagerRest();
            configManager = new ConfigManagerRest();
            configResolver = new ConfigResolverRest();
            commandRunner = new CommandRunnerRest();
            actionResolver = new ActionResolverRest();
            clinicianResolver = new ClinicianResolverRest();
            administratorResolver = new AdministratorResolverRest();
            fileResolver = new FileResolverRest();
            imageManager = new ImageManagerRest();
        } else if (storageType == DataStorageType.MEMORY) {
            clientManager = new ClientManagerMemory();
            clientResolver = new ClientResolverMemory();
            clinicianManager = new ClinicianManagerMemory();
            administratorManager = new AdministratorManagerMemory();
            authenticationManager = new AuthenticationManagerMemory();
            configManager = new ConfigManagerMemory();
            configResolver = new ConfigResolverMemory();
            commandRunner = commandText -> {
                throw new UnsupportedOperationException("Memory storage type does not support running commands.");
            };
            actionResolver = new ActionResolverMemory();
            clinicianResolver = new ClincianResolverMemory();
            administratorResolver = new AdministratorResolverMemory();
            fileResolver = new FileResolverMemory();
            imageManager = new ImageManagerMemory();
        } else {
            throw new IllegalArgumentException("DataStorageType cannot be null.");
        }
    }

    public static String getBaseUri() {
        return baseUri;
    }

    public static void setBaseUri(String uri) {
        baseUri = uri;
    }

    public static void login(Client client) {
        session = new Session(client);
    }

    public static void login(Clinician clinician) {
        session = new Session(clinician);
    }

    public static void login(Administrator administrator) {
        session = new Session(administrator);
    }

    public static boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    public static void setUnsavedChanges(boolean changes) {
        unsavedChanges = changes;
    }

    public static Set<Country> getAllowedCountries() {
        return allowedCountries;
    }

    public static void setAllowedCountries(Set<Country> countries) {
        allowedCountries = countries;
    }

    public static void logout() {
        // Do something with the old session
        session = null;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String t) {
        token = t;
    }

    public static void reset() {
        init(currentStorageType);

        logout();
        unsavedChanges = false;
        mainControllers = new ArrayList<>();
    }

    public static void addMainController(MainController mainController) {
        mainControllers.add(mainController);
    }

    public static List<MainController> getMainControllers() {
        return Collections.unmodifiableList(mainControllers);
    }

    public static void clearMainControllers() {
        mainControllers.clear();
    }

    public static void deleteMainController(MainController controller) {
        mainControllers.remove(controller);
    }

    private static MappingJackson2HttpMessageConverter customConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(customObjectMapper());
        return converter;
    }

    public static ObjectMapper customObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
        return mapper;
    }

    public static String getClientEtag() {
        //return clientEtag;
        return "\"\""; // just use a valid etag, the server doesn't care if it is correct
    }

    public static void setClientEtag(String etag) {
        recentEtag = etag;
        clientEtag = etag;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        State.primaryStage = primaryStage;
    }

    public static String getClinicianEtag() {
        //return clinicianEtag;
        return "\"\""; // just use a valid etag, the server doesn't care if it is correct
    }

    public static void setClinicianEtag(String etag) {
        recentEtag = etag;
        clinicianEtag = etag;
    }

    public static String getAdministratorEtag() {
        //return administratorEtag;
        return "\"\""; // just use a valid etag, the server doesn't care if it is correct
    }

    public static void setAdministratorEtag(String etag) {
        recentEtag = etag;
        administratorEtag = etag;
    }

    public static String getRecentEtag() {
        return recentEtag;
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static void setRestTemplate(RestTemplate template) {
        restTemplate = template;
    }

    public static ClientManager getClientManager() {
        return clientManager;
    }

    public static ClinicianManager getClinicianManager() {
        return clinicianManager;
    }

    public static AdministratorManager getAdministratorManager() {
        return administratorManager;
    }

    public static AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static ConfigResolver getConfigResolver() {
        return configResolver;
    }

    public static Session getSession() {
        return session;
    }

    public static ClientResolver getClientResolver() {
        return clientResolver;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public static ActionResolver getActionResolver() {
        return actionResolver;
    }

    public static ClinicianResolver getClinicianResolver() {
        return clinicianResolver;
    }

    public static AdministratorResolver getAdministratorResolver() {
        return administratorResolver;
    }

    public static FileResolver getFileResolver() {
        return fileResolver;
    }

    public static ImageManager getImageManager() {
        return imageManager;
    }

    public static Clinician getViewedClinician() {
        return viewedClinician;
    }

    public static void setViewedClinician(Clinician viewedClinician) {
        State.viewedClinician = viewedClinician;
    }

    public static UiType getUiType() {
        return uiType;
    }

    public static void setUiType(UiType type) {
        uiType = type;
    }

    public static boolean isUseHackyMouseTouch() {
        return useHackyMouseTouch;
    }

    public static void setUseHackyMouseTouch(boolean useHackyMouseTouch) {
        State.useHackyMouseTouch = useHackyMouseTouch;
    }

    public static Client getSpiderwebDonor() {
        return spiderwebDonor;
    }

    public static void setSpiderwebDonor(Client spiderwebDonor) {
        State.spiderwebDonor = spiderwebDonor;
    }
}
