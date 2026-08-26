package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.domain.DomainException;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.adapters.outbound.CryptoUtils;
import com.dafi.desktop.shared.utils.JsonObjectReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Outbound adapter implementing {@link ClientRepositoryPort}; it persists
 * clients as an AES-GCM encrypted JSON file (clients.json) in the application
 * data directory (typically ~/.dafi/data), mapping fields manually with Gson.
 */
public class JsonClientRepositoryAdapter implements ClientRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JsonClientRepositoryAdapter.class);

    private static final String ARRAY_KEY = "clients";
    private static final String FILE_NAME = "clients.json";

    private final Path filePath;
    private final CryptoUtils cryptoUtils;
    private final Gson gson;

    /**
     * Creates the adapter.
     *
     * @param dataDirectory directory where clients.json is stored
     * @param cryptoUtils   helper used to encrypt and decrypt the file contents
     */
    public JsonClientRepositoryAdapter(Path dataDirectory, CryptoUtils cryptoUtils) {
        this.filePath = dataDirectory.resolve(FILE_NAME);
        this.cryptoUtils = cryptoUtils;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Loads and decrypts the client file, returning every stored client.
     *
     * @return all persisted clients, or an empty list if the file does not exist yet
     */
    @Override
    public List<Client> findAll() {
        String json = cryptoUtils.loadEncryptedData(filePath);
        if (json == null) {
            return new ArrayList<>();
        }
        return parseClients(json);
    }

    /**
     * Inserts or updates a client, replacing any existing client with the same
     * id, and rewrites the whole encrypted file.
     *
     * @param client client to persist
     */
    @Override
    public void save(Client client) {
        List<Client> clients = findAll();
        clients.removeIf(existing -> existing.getId().equals(client.getId()));
        clients.add(client);
        saveAll(clients);
    }

    /**
     * Serializes the given clients and writes them to the encrypted JSON file,
     * replacing its previous contents.
     *
     * @param clients clients to persist
     */
    @Override
    public void saveAll(List<Client> clients) {
        JsonArray clientsArray = new JsonArray();
        for (Client client : clients) {
            clientsArray.add(toJson(client));
        }

        JsonObject wrapper = new JsonObject();
        wrapper.add(ARRAY_KEY, clientsArray);

        cryptoUtils.saveEncryptedData(gson.toJson(wrapper), filePath);
    }

    private List<Client> parseClients(String json) {
        try {
            JsonObject wrapper = JsonParser.parseString(json).getAsJsonObject();
            if (!wrapper.has(ARRAY_KEY) || wrapper.get(ARRAY_KEY).isJsonNull()) {
                log.warn("Clave '{}' no encontrada en el archivo JSON de clientes", ARRAY_KEY);
                return new ArrayList<>();
            }
            JsonArray clientsArray = wrapper.getAsJsonArray(ARRAY_KEY);

            List<Client> clients = new ArrayList<>();
            for (JsonElement element : clientsArray) {
                appendIfValid(clients, element.getAsJsonObject());
            }
            return clients;
        } catch (Exception e) {
            log.error("Error al parsear el JSON de clientes, retornando lista vacía", e);
            return new ArrayList<>();
        }
    }

    /**
     * Deserializes a single record and appends it to the result list.
     * Records violating domain invariants are skipped and logged instead of
     * aborting the whole load, keeping the application usable when a corrupt
     * or legacy record exists in the file.
     */
    private void appendIfValid(List<Client> clients, JsonObject obj) {
        try {
            clients.add(fromJson(obj));
        } catch (DomainException e) {
            log.error("Registro de cliente inválido omitido ({}). Detalle: {}",
                    obj.get("id"), e.getMessage());
        }
    }

    private Client fromJson(JsonObject obj) {
        return Client.builder()
                .id(JsonObjectReader.getStringOrEmpty(obj, "id"))
                .contractFolio(JsonObjectReader.getStringOrEmpty(obj, "contractFolio"))
                .fullName(JsonObjectReader.getStringOrEmpty(obj, "fullName"))
                .ine(JsonObjectReader.getStringOrEmpty(obj, "ine"))
                .contractType(JsonObjectReader.getStringOrEmpty(obj, "contractType"))
                .address(JsonObjectReader.getStringOrEmpty(obj, "address"))
                .neighborhood(JsonObjectReader.getStringOrEmpty(obj, "neighborhood"))
                .phone(JsonObjectReader.getStringOrEmpty(obj, "phone"))
                .email(JsonObjectReader.getStringOrEmpty(obj, "email"))
                .paymentMethod(JsonObjectReader.getStringOrEmpty(obj, "paymentMethod"))
                .firstBeneficiary(JsonObjectReader.getStringOrEmpty(obj, "firstBeneficiary"))
                .secondBeneficiary(JsonObjectReader.getStringOrEmpty(obj, "secondBeneficiary"))
                .saleDescription(JsonObjectReader.getStringOrEmpty(obj, "saleDescription"))
                .annuity(JsonObjectReader.getStringOrEmpty(obj, "annuity"))
                .block(JsonObjectReader.getStringOrEmpty(obj, "block"))
                .lot(JsonObjectReader.getStringOrEmpty(obj, "lot"))
                .managementFee(JsonObjectReader.getBigDecimalOrZero(obj, "managementFee"))
                .advance(JsonObjectReader.getBigDecimalOrZero(obj, "advance"))
                .totalBalance(JsonObjectReader.getBigDecimalOrZero(obj, "totalBalance"))
                .firstPaymentDate(JsonObjectReader.getDateOrNull(obj, "firstPaymentDate"))
                .contractDate(JsonObjectReader.getDateOrNull(obj, "contractDate"))
                .paymentDay(JsonObjectReader.getIntOrZero(obj, "paymentDay"))
                .totalPayments(JsonObjectReader.getIntOrZero(obj, "totalPayments"))
                .monthlyPayment(JsonObjectReader.getBigDecimalOrZero(obj, "monthlyPayment"))
                .contractEndDate(JsonObjectReader.getDateOrNull(obj, "contractEndDate"))
                .build();
    }

    private JsonObject toJson(Client client) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", client.getId());
        obj.addProperty("contractFolio", orEmpty(client.getContractFolio()));
        obj.addProperty("fullName", orEmpty(client.getFullName()));
        obj.addProperty("ine", orEmpty(client.getIne()));
        obj.addProperty("contractType", orEmpty(client.getContractType()));
        obj.addProperty("address", orEmpty(client.getAddress()));
        obj.addProperty("neighborhood", orEmpty(client.getNeighborhood()));
        obj.addProperty("phone", orEmpty(client.getPhone()));
        obj.addProperty("email", orEmpty(client.getEmail()));
        obj.addProperty("paymentMethod", orEmpty(client.getPaymentMethod()));
        obj.addProperty("firstBeneficiary", orEmpty(client.getFirstBeneficiary()));
        obj.addProperty("secondBeneficiary", orEmpty(client.getSecondBeneficiary()));
        obj.addProperty("saleDescription", orEmpty(client.getSaleDescription()));
        obj.addProperty("annuity", orEmpty(client.getAnnuity()));
        obj.addProperty("block", orEmpty(client.getBlock()));
        obj.addProperty("lot", orEmpty(client.getLot()));
        obj.addProperty("managementFee", decimalOrZero(client.getManagementFee()));
        obj.addProperty("advance", decimalOrZero(client.getAdvance()));
        obj.addProperty("totalBalance", decimalOrZero(client.getTotalBalance()));
        obj.addProperty("firstPaymentDate", dateOrNull(client.getFirstPaymentDate()));
        obj.addProperty("contractDate", dateOrNull(client.getContractDate()));
        obj.addProperty("paymentDay", client.getPaymentDay());
        obj.addProperty("totalPayments", client.getTotalPayments());
        obj.addProperty("monthlyPayment", decimalOrZero(client.getMonthlyPayment()));
        obj.addProperty("contractEndDate", dateOrNull(client.getContractEndDate()));
        return obj;
    }

    private String orEmpty(String value) {
        return value != null ? value : "";
    }

    private String decimalOrZero(BigDecimal value) {
        return value != null ? value.toPlainString() : "0";
    }

    private String dateOrNull(java.time.LocalDate value) {
        return value != null ? value.toString() : "";
    }
}
