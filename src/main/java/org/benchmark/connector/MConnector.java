package org.benchmark.connector;

import de.pseudonymisierung.mainzelliste.client.ID;
import de.pseudonymisierung.mainzelliste.client.InvalidSessionException;
import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface MConnector {
    // TODO- verify return types

        /** Prepares the benchmark environment. */
        public void prepare() throws BenchmarkException;


        /**
         * Add patient.
         */
        public JSONObject addPatient(String IdType, String IdString) throws BenchmarkException, InvalidSessionException, MainzellisteNetworkException, JSONException;

        /** Read patient. */
        public String readPatient(ID patientId) throws BenchmarkException, InvalidSessionException, MainzellisteNetworkException;

        /** Edit patient. */
        public String editPatient(ID patientId) throws BenchmarkException, InvalidSessionException, MainzellisteNetworkException, JSONException;

        /** Delete atient. */
        public String deletePatient(ID patientId) throws BenchmarkException, InvalidSessionException, MainzellisteNetworkException;


        public int ping() throws BenchmarkException, MainzellisteNetworkException;
}
