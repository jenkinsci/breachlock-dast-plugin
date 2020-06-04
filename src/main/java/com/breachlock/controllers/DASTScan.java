package com.breachlock.controllers;

import com.breachlock.helpers.ApiHelper;
import com.breachlock.models.AssetModel;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import jenkins.model.Jenkins;
import java.util.ArrayList;
import java.util.Iterator;
import okhttp3.FormBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.apache.commons.validator.EmailValidator;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author mitchel.k@breachlock.com
 */
public class DASTScan extends Builder implements SimpleBuildStep {

    private Secret apikey;
    private String asset;
    private String email;
    
    @DataBoundConstructor
    public DASTScan() {
        
    }

    @Override
    /**
     * Get the form descriptor
     * 
     * @return DescriptorImpl
     */
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
        
    @Override
    /**
     * Runs on build of a project and starts a "Live Scan".
     */
    public void perform(
            Run<?, ?> run, FilePath workspace, 
            Launcher launcher, TaskListener listener) 
            throws InterruptedException, IOException {
        
        // Update console on scan started
        StringBuilder strBuilder = new StringBuilder("");
        strBuilder.append("Starting scan for for asset: ");
        strBuilder.append(this.asset);
        
        listener.getLogger().println(strBuilder.toString());
        
        // Fetch assets
        ApiHelper apiHelper = new ApiHelper();
        apiHelper.setEndpoint("/servers/runlivescanForJenkins");
        apiHelper.setFormBody(
            new FormBody.Builder()
                .add("token", this.getApikey())
                .add("hostid", this.asset)
                .build()
        );

        String response = apiHelper.postRequest();
        
        strBuilder.delete(0, strBuilder.length());
        if (response.compareTo("1") == 0)
            strBuilder.append("Scan started!");
        else
            strBuilder.append("Scan could not be started!");
        
        listener.getLogger().println(strBuilder.toString());
    }
    
    /**
     * Return asset.
     * 
     * This is required for functioning of the form
     * 
     * @return asset Asset to scan
     */
    public String getAsset() {
        return this.asset;
    }

    /**
     * Set the asset to scan.This is required for functioning of the form
     *
     * @param asset Asset to scan
     */
    @DataBoundSetter
    public void setAsset(String asset) {
        this.asset = asset;
    }
    
    /**
     * Return the email.
     * 
     * This is required for functioning of the form
     * 
     * @return email Email used for app.breachlock.co
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Set the email.This is required for functioning of the form
     * 
     * @param email Email used for app.breachlock.co
     */
    @DataBoundSetter
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Return the encrypted secret API key.
     * 
     * This is required for functioning of the form
     * 
     * @return apiKey Jenkins integration API key
     */
    public String getApikey() {
        if (this.apikey == null)
            this.apikey = getDescriptor().getApikey();
        
        return this.apikey.getPlainText();
    }
    
    /**
     * Set the encrypted API key.
     * 
     * @param apikey Jenkins integration API key
     */
    @DataBoundSetter
    public void setApikey(Secret apikey) {
        this.apikey = apikey;
    }
     
    /**
     * Form descriptor used for form handling.
     */
    @Symbol("DASTScan")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        // https://www.jenkins.io/doc/developer/security/secrets/
        private Secret apikey;
         
        /**
         * Provide access to the API key from DASTScan
         */
        public DescriptorImpl() {
            super(DASTScan.class);
            load();
        }
        
        /**
         * Return the encrypted secret API key.
         * 
         * This is required for functioning of the form
         * 
         * @return apiKey Jenkins integration API key
         */
        public Secret getApikey() {
            return this.apikey;
        }

        /**
         * Encode and set the string API key.
         * 
         * This is required for functioning of the form
         * 
         * @param apikey Jenkins integration API key
         */
        public void setApikey(String apikey) {
            this.apikey = Secret.fromString(apikey);
        }
        
        /**
         * Check the email field.
         * 
         * @param email Email used for app.breachlock.com
         * @param item
         * 
         * @return FormValidation
         * 
         * @throws IOException
         * @throws ServletException 
         */
        public FormValidation doCheckEmail(
                @QueryParameter String email,
                @AncestorInPath Item item)
                throws IOException, ServletException {
            
            // Check permissions
            item.checkPermission(Item.CONFIGURE);
            
            // Check form fields
            if (Util.fixEmptyAndTrim(email) == null)
                return FormValidation.error(Messages.DASTScan_DescriptorImpl_errors_missingEmail());
            else if (Util.fixEmptyAndTrim(email) != null && EmailValidator.getInstance().isValid(email) == false)
                return FormValidation.error(Messages.DASTScan_DescriptorImpl_warnings_invalidEmail());
            else
                return FormValidation.ok();
        }
        
        /**
         * Check the asset field.
         * 
         * @param asset Asset to scan
         * @param item
         * 
         * @return FormValidation
         * 
         * @throws IOException
         * @throws ServletException 
         */
        @POST
        public FormValidation doCheckAsset(
                @QueryParameter String asset,
                @AncestorInPath Item item)
                throws IOException, ServletException {
                        
            // Check permissions           
            item.checkPermission(Item.CONFIGURE);
            
            // Check form fields
            if (Util.fixEmptyAndTrim(asset) == null)
                return FormValidation.error(Messages.DASTScan_DescriptorImpl_errors_missingAsset());
            else
                return FormValidation.ok();
        }
        
        /**
         * Check the API key field.
         * 
         * @param apikey Jenkins integration API key
         * @param item
         * 
         * @return FormValidation
         * 
         * @throws IOException
         * @throws ServletException 
         */
        @SuppressWarnings("unused")
        public FormValidation doCheckApikey(
                @QueryParameter String apikey,
                @AncestorInPath Item item)
                throws IOException, ServletException {
            
            // Check permissions
            item.checkPermission(Item.CONFIGURE);
            
            // Check form fields
            if (Util.fixEmptyAndTrim(apikey) == null)
                return FormValidation.error(Messages.DASTScan_DescriptorImpl_errors_missingAPIKey());
            else if (apikey.length() < 32 )
                return FormValidation.warning(Messages.DASTScan_DescriptorImpl_warnings_tooShort());
            else
                return FormValidation.ok();
        }
        
        /**
         * Register the plugin to the Breachlock Platform.
         * 
         * @param apikey Jenkins integration API key
         * @param asset Asset to scan
         * @param email Email used for app.breachlock.com
         * 
         * @return FormValidation
         * 
         * @throws IOException
         * @throws ServletException 
         */
        @SuppressWarnings("unused")
        @POST
        public FormValidation doTestConnection(
                @QueryParameter String apikey,
                @QueryParameter String asset,
                @QueryParameter String email)
                throws IOException, ServletException {
            
            // Check permissions
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            
            // Register Plugin
            ApiHelper apiHelper = new ApiHelper();
            apiHelper.setEndpoint("/servers/registerAssetForJenkins");
            apiHelper.setFormBody(
                new FormBody.Builder()
                    .add("token", apikey)
                    .add("emailid", email)
                    .add("hostid", asset)
                    .build()
            );
            
            String response = apiHelper.postRequest();
            if (response.compareTo("1") == 0)
                return FormValidation.ok("Success!");
            else
                return FormValidation.error("Plugin could not be registered, or is already registered. Contact support if the problem persists.");
        }
        
        /**
         * Fill the selectbox of with assets supplied by the Breachlock Platform.
         * 
         * @param apikey Jenkins integration API key
         * @param email Email used for app.breachlock.com
         * 
         * @return ListBoxModel Selectbox with assets
         */
        @SuppressWarnings("unused")
        @POST
        public ListBoxModel doFillAssetItems(
                @QueryParameter String apikey,
                @QueryParameter String email) {  
            
            // https://wiki.jenkins.io/display/JENKINS/Matrix-based+security
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            
            // Fetch assets
            ApiHelper apiHelper = new ApiHelper();
            apiHelper.setEndpoint("/servers/getAssetsForJenkins");
            apiHelper.setFormBody(
                new FormBody.Builder()
                    .add("token", apikey)
                    .add("emailid", email)
                    .build()
            );
            
            String response = apiHelper.postRequest();
            JSONArray jsonResponse = apiHelper.parseJSON(response);
            
            // Loop through assets
            Iterator<JSONObject> it = jsonResponse.iterator();
            AssetModel assetModel = new AssetModel();
            
            while (it.hasNext()) {
                JSONObject obj = it.next();
                
                assetModel.addAsset(new AssetModel.Asset(
                    obj.get("hostname").toString(),
                    obj.get("hostid").toString(),
                    obj.get("org_id").toString()
                ));
            }

            // Fill selectbox
            ListBoxModel model = new ListBoxModel();
            
            final ArrayList<AssetModel.Asset> assets = assetModel.getAssets();
            model.add("Please select an asset", "");
            
            for (AssetModel.Asset a : assets)
                model.add(a.getUrl(), a.getHostId());

            return model;
        }

        /**
         * Always allow this class to be used as a "build step" for a job.
         * 
         * @param aClass
         * @return 
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * Name to display on top of the form and as plugin name.
         * 
         * @return Display Name
         */
        @Override
        public String getDisplayName() {
            return Messages.DASTScan_DescriptorImpl_DisplayName();
        }
    }
}
