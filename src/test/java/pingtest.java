//
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import mainzelliste.org.benchmark.BenchmarkException;
//import trustdeck.mainzelliste.org.benchmark.TConnector;
//import trustdeck.mainzelliste.org.benchmark.TrustDeckConnectorFactory;
//import org.trustdeck.client.TrustDeckClient;
//import org.trustdeck.client.exception.TrustDeckClientLibraryException;
//import org.trustdeck.client.exception.TrustDeckResponseException;
//import org.trustdeck.client.model.Domain;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import org.trustdeck.client.model.IdentifierItem;
//public class pingtest {
//
//
//    private TrustDeckClient trustDeckClient;
//    private Domain domain;
//    private TConnector mainzelliste;
//
//    @BeforeEach
//    void setUp() throws BenchmarkException {
//        TrustDeckConnectorFactory factory = new TrustDeckConnectorFactory();
//        this.mainzelliste = factory.create();
//        this.trustDeckClient = mainzelliste.getTrustDeckClient();
//        this.domain = Domain.builder().name("Project-Alpha3").prefix("PA-").build();
//    }
//
//    @Test
//    void testPing() {
//        assertDoesNotThrow(() -> {
//            mainzelliste.ping();
//        });
//    }
//
//    @Test
//    void createDomain() {
//
//        try {
//            this.trustDeckClient.domains().create(domain);
//        } catch (Exception e) {
//            throw new TrustDeckClientLibraryException("Creating domain '" + domain.getName() + "' failed: " + e.getMessage());
//        }
//
//    }
////
////
////    @Test
////    void createPseudonym() {
////        IdentifierItem identifierItem = IdentifierItem.builder()
////                .identifier("TestID-" + System.currentTimeMillis())
////                .idType("TestType")
////                .build();
////
////        Pseudonym pseudonym = Pseudonym.builder()
////                .identifierItem(identifierItem)
////                .build();
////
////        try {
////            IdentifierItem identifierItem1 = IdentifierItem.builder().identifier("TestID2").idType("TestType").build();
////            // Create new pseudonym by only providing the identifier item
////            Pseudonym createdPseudonym = this.trustDeckClient.pseudonyms("Project-Alpha3").create(identifierItem1, false);
////            System.out.print("Create Pseudonym  '" + createdPseudonym + "'  successfully");
////
////        } catch (Exception e) {
////            e.printStackTrace();
////            throw new TrustDeckClientLibraryException("Creating Pseudonym '" + pseudonym + "' Failed", e);
////        }
////    }
////
////    @Test
////    void testReadPseudonym() {
////
////        IdentifierItem identifierItem1 = IdentifierItem.builder().identifier("TestID2").idType("TestType").build();
////        try {
////            Pseudonym readPseudonym = this.trustDeckClient.pseudonyms("Project-Alpha3").get(identifierItem1);
////            System.out.print("Create Pseudonym successfully  ");
////        } catch (Exception e) {
////            e.printStackTrace();
////            throw new TrustDeckClientLibraryException("Fetching Pseudonym with ID'" + identifierItem1.getIdentifier() + "' Failed", e);
////        }
////    }
////
////    // In pingtest.java
////
////    @Test
////    void testUpdatePseudonym() {
////        IdentifierItem identifierItem = IdentifierItem.builder().identifier("TestID2").idType("TestType").build();
////        try {
////            this.trustDeckClient.pseudonyms("Project-Alpha3").update(identifierItem, Pseudonym.builder().validFrom(LocalDateTime.now()).build());
////            System.out.print("Update Pseudonym '" + identifierItem.getIdentifier() + "' successfully");
////        } catch (Exception e) {
////            e.printStackTrace();
////            throw new TrustDeckClientLibraryException("Updating Pseudonym with ID '" + identifierItem.getIdentifier() + "' Failed", e);
////        }
////    }
//
//    @Test
//    void testDeletePseudonym() throws BenchmarkException {
//        IdentifierItem identifierItem1 = IdentifierItem.builder().identifier("TestID2").idType("TestType").build();
//        try {
//            boolean deleted = this.trustDeckClient.pseudonyms("Project-Alpha3").delete(identifierItem1);
//            System.out.print("Delete Pseudonym result: " + deleted);
//        } catch (TrustDeckClientLibraryException | TrustDeckResponseException e) {
//            throw new BenchmarkException("Deleting Pseudonym with ID '" + identifierItem1.getIdentifier() + "' Failed", e);
//        }
//      }
//
//      @Test
//      void testGetStorage() throws BenchmarkException {
//              try {
//                  this.trustDeckClient.dbMaintenance().getStorage("domain");
//              } catch (TrustDeckClientLibraryException | TrustDeckResponseException e) {
//                  throw new BenchmarkException("Creating domain '" + domain.getName() + "' failed: " ,e);
//              }
//      }
////
////    @Test
////    void deleteDomain() {
////        try {
////            this.trustDeckClient.domains().delete("TestStudie-MPADemo",true);
////        } catch (Exception e) {
////            throw new TrustDeckClientLibraryException("Deleting domain '" + domain.getName() + "' failed: " + e.getMessage());
////        }
//
//
//}
//
