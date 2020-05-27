package com.breachlock.models;

import java.util.ArrayList;

/**
 * @author mitchel.k@breachlock.com
 */
public final class AssetModel {
    private final ArrayList<Asset> assets;
    
    public static class Asset {
        private final String url;
        private String hostId;
        private String orgId;
        
        public Asset(String url, String hostId, String orgId) {
            this.url = url;
            this.hostId = hostId;
            this.orgId = orgId;
        }
        
        public String getUrl() {
            return this.url;
        }
        
        public String getHostId() {
            return this.hostId;
        }
        
        public String getOrgId() {
            return this.orgId;
        }
    }

    public AssetModel() {      
        this.assets = new ArrayList<>();
    }

    public void addAsset(Asset asset) {
        this.assets.add(asset);
    }

    public ArrayList<Asset> getAssets() {
        return this.assets;
    }
}
