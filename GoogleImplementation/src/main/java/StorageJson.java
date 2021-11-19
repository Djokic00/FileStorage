public class StorageJson {
    private String storageName;
    private String userId;
    private String configId;
    private String storageId;
    private String storageJsonId;

    public StorageJson(String storageName, String userId, String configId, String storageId, String storageJsonId) {
        this.storageName = storageName;
        this.userId = userId;
        this.configId = configId;
        this.storageId = storageId;
        this.storageJsonId = storageJsonId;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageJsonId() {
        return storageJsonId;
    }

    public void setStorageJsonId(String storageJsonId) {
        this.storageJsonId = storageJsonId;
    }
}
