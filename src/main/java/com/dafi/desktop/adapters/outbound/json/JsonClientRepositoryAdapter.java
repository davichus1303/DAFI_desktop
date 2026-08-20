package com.dafi.desktop.adapters.outbound.json;

import com.dafi.desktop.application.client.ClientRepositoryPort;
import com.dafi.desktop.application.security.EncryptionPort;
import com.dafi.desktop.application.security.KeyStoragePort;
import com.dafi.desktop.domain.client.Client;
import com.dafi.desktop.domain.client.ContractType;
import com.dafi.desktop.domain.client.PaymentMethod;
import com.google.gson.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de persistencia de clientes utilizando JSON cifrado.
 */
public class JsonClientRepositoryAdapter implements ClientRepositoryPort {

    private final Path filePath;
    private final EncryptionPort encryption;
    private final KeyStoragePort keyStorage;
    private final Gson gson;

    public JsonClientRepositoryAdapter(Path dataDirectory, EncryptionPort encryption,
                                       KeyStoragePort keyStorage) {
        this.filePath = dataDirectory.resolve("clients.json");
        this.encryption = encryption;
        this.keyStorage = keyStorage;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Client> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String encryptedContent = Files.readString(filePath);
            String key = keyStorage.getEncryptionKey();

            if (key == null) {
                throw new RuntimeException("No se encontró la clave de cifrado");
            }

            String json = encryption.decrypt(encryptedContent, key);
            return parseClients(json);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de clientes", e);
        }
    }

    @Override
    public void save(Client client) {
        List<Client> clients = findAll();
        clients.removeIf(c -> c.getId().equals(client.getId()));
        clients.add(client);
        saveAll(clients);
    }

    @Override
    public void saveAll(List<Client> clients) {
        try {
            String key = keyStorage.getEncryptionKey();
            if (key == null) {
                throw new RuntimeException("No se encontró la clave de cifrado");
            }

            JsonArray clientsArray = new JsonArray();
            for (Client client : clients) {
                clientsArray.add(clientToJson(client));
            }

            JsonObject wrapper = new JsonObject();
            wrapper.add("clients", clientsArray);

            String json = gson.toJson(wrapper);
            String encrypted = encryption.encrypt(json, key);

            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, encrypted);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar clientes", e);
        }
    }

    private List<Client> parseClients(String json) {
        JsonObject wrapper = JsonParser.parseString(json).getAsJsonObject();
        JsonArray clientsArray = wrapper.getAsJsonArray("clients");

        List<Client> clients = new ArrayList<>();
        for (JsonElement element : clientsArray) {
            clients.add(jsonToClient(element.getAsJsonObject()));
        }
        return clients;
    }

    private Client jsonToClient(JsonObject obj) {
        return new Client(
                getStringOrEmpty(obj, "id"),
                getStringOrEmpty(obj, "contractFolio"),
                getStringOrEmpty(obj, "fullName"),
                getStringOrEmpty(obj, "ine"),
                getEnumOrEmpty(obj, "contractType", ContractType.class),
                getStringOrEmpty(obj, "address"),
                getStringOrEmpty(obj, "neighborhood"),
                getStringOrEmpty(obj, "phone"),
                getStringOrEmpty(obj, "email"),
                getEnumOrEmpty(obj, "paymentMethod", PaymentMethod.class),
                getStringOrEmpty(obj, "firstBeneficiary"),
                getStringOrEmpty(obj, "secondBeneficiary"),
                getStringOrEmpty(obj, "saleDescription"),
                getStringOrEmpty(obj, "annuality"),
                getStringOrEmpty(obj, "block"),
                getStringOrEmpty(obj, "lot"),
                getBigDecimalOrZero(obj, "managementFee"),
                getBigDecimalOrZero(obj, "advance"),
                getBigDecimalOrZero(obj, "totalBalance"),
                getDateOrEmpty(obj, "firstPaymentDate"),
                getDateOrEmpty(obj, "contractDate"),
                getIntOrZero(obj, "paymentDay"),
                getIntOrZero(obj, "totalPayments"),
                getBigDecimalOrZero(obj, "monthlyPayment"),
                getDateOrNull(obj, "contractEndDate")
        );
    }

    private JsonObject clientToJson(Client client) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", client.getId());
        obj.addProperty("contractFolio", client.getContractFolio() != null ? client.getContractFolio() : "");
        obj.addProperty("fullName", client.getFullName() != null ? client.getFullName() : "");
        obj.addProperty("ine", client.getIne() != null ? client.getIne() : "");
        obj.addProperty("contractType", client.getContractType() != null ? client.getContractType().name() : "");
        obj.addProperty("address", client.getAddress() != null ? client.getAddress() : "");
        obj.addProperty("neighborhood", client.getNeighborhood() != null ? client.getNeighborhood() : "");
        obj.addProperty("phone", client.getPhone() != null ? client.getPhone() : "");
        obj.addProperty("email", client.getEmail() != null ? client.getEmail() : "");
        obj.addProperty("paymentMethod", client.getPaymentMethod() != null ? client.getPaymentMethod().name() : "");
        obj.addProperty("firstBeneficiary", client.getFirstBeneficiary() != null ? client.getFirstBeneficiary() : "");
        obj.addProperty("secondBeneficiary", client.getSecondBeneficiary() != null ? client.getSecondBeneficiary() : "");
        obj.addProperty("saleDescription", client.getSaleDescription() != null ? client.getSaleDescription() : "");
        obj.addProperty("annuality", client.getAnnuity() != null ? client.getAnnuity() : "");
        obj.addProperty("block", client.getBlock() != null ? client.getBlock() : "");
        obj.addProperty("lot", client.getLot() != null ? client.getLot() : "");
        obj.addProperty("managementFee", client.getManagementFee() != null ? client.getManagementFee().toString() : "0");
        obj.addProperty("advance", client.getAdvance() != null ? client.getAdvance().toString() : "0");
        obj.addProperty("totalBalance", client.getTotalBalance() != null ? client.getTotalBalance().toString() : "0");
        obj.addProperty("firstPaymentDate", client.getFirstPaymentDate() != null ? client.getFirstPaymentDate().toString() : "");
        obj.addProperty("contractDate", client.getContractDate() != null ? client.getContractDate().toString() : "");
        obj.addProperty("paymentDay", client.getPaymentDay());
        obj.addProperty("totalPayments", client.getTotalPayments());
        obj.addProperty("monthlyPayment", client.getMonthlyPayment() != null ? client.getMonthlyPayment().toString() : "0");
        obj.addProperty("contractEndDate", client.getContractEndDate() != null ? client.getContractEndDate().toString() : "");
        return obj;
    }

    private String getStringOrEmpty(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private <E extends Enum<E>> E getEnumOrEmpty(JsonObject obj, String key, Class<E> enumClass) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalOrZero(JsonObject obj, String key) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate getDateOrEmpty(JsonObject obj, String key) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private LocalDate getDateOrNull(JsonObject obj, String key) {
        String value = getStringOrEmpty(obj, key);
        if (value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private int getIntOrZero(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }
}
