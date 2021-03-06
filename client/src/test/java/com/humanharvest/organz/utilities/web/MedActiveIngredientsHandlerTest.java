package com.humanharvest.organz.utilities.web;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.humanharvest.organz.BaseTest;

import com.google.api.client.http.HttpTransport;
import org.junit.Test;

public class MedActiveIngredientsHandlerTest extends BaseTest {

    private HttpTransport mockTransport;
    private MedActiveIngredientsHandler handler;

    @Test
    public void getActiveIngredients1() throws Exception {
        MockCacheManager.Create();

        final String EXPECTED_RESPONSE_BODY = "[\"Hydralazine hydrochloride; hydrochlorothiazide; reserpine\",\"Hydroch"
                + "lorothiazide; reserpine\",\"Hydroflumethiazide; reserpine\",\"Reserpine\"]";

        mockTransport = MockHelper.makeMockHttpTransport(EXPECTED_RESPONSE_BODY);
        handler = new MedActiveIngredientsHandler(mockTransport);

        List<String> expected = Arrays
                .asList("Hydralazine hydrochloride; hydrochlorothiazide; reserpine", "Hydrochlorothiazide; reserpine",
                        "Hydroflumethiazide; reserpine", "Reserpine");

        List<String> actual = Collections.emptyList();
        actual = handler.getActiveIngredients("reserpine");

        assertEquals(expected, actual);
    }
}
