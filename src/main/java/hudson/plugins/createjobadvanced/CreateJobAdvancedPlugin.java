package hudson.plugins.createjobadvanced;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import jenkins.model.GlobalConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * @author Bertrand Gressier
 *
 */
@Extension
public class CreateJobAdvancedPlugin extends GlobalConfiguration {

    private static final Logger log = Logger.getLogger(CreateJobAdvancedPlugin.class.getName());

    private boolean autoOwnerRights;
    private boolean autoPublicBrowse;
    private boolean replaceSpace;

    private boolean activeLogRotator;
    private int daysToKeep = -1;
    private int numToKeep = -1;
    private int artifactDaysToKeep = -1;
    private int artifactNumToKeep = -1;

    private boolean activeDynamicPermissions;
    private String extractPattern;

    private boolean mvnArchivingDisabled;
    private boolean mvnPerModuleEmail;

    private List<DynamicPermissionConfig> dynamicPermissionConfigs = new ArrayList<>();

    /** @return the singleton instance */
    public static CreateJobAdvancedPlugin get() {
        return ExtensionList.lookupSingleton(CreateJobAdvancedPlugin.class);
    }

    public CreateJobAdvancedPlugin() {
        log.info("Create job advanced plugin started ...");
        load();
    }

    // @Override
    // public void start() throws Exception {
    //     super.start();
    //     
    //     load();
    // }
        


    // @Override
    // public boolean configure(StaplerRequest2 req, JSONObject formData)
    //         throws FormException {

    //     boolean result = false;

    //     // autoOwnerRights = formData.optBoolean("security", false);
    //     // autoPublicBrowse = formData.optBoolean("public", false);
    //     // replaceSpace = formData.optBoolean("jobspacesinname", false);

    //     // mvnArchivingDisabled = formData.optBoolean("mvnArchivingDisabled", false);
    //     // mvnPerModuleEmail = formData.optBoolean("mvnPerModuleEmail", false);

    //     //final JSONObject activeLogRotatorJson = formData.optJSONObject("activeLogRotator");

    //     final JSONObject activeDynamicPermissionsJson = formData.optJSONObject("activeDynamicPermissions");

    //     if (activeDynamicPermissionsJson != null) {
    //         activeDynamicPermissions = true;
    //         //extractPattern = activeDynamicPermissionsJson.optString("extractPattern", "");

    //         dynamicPermissionConfigs.clear();
    //         final Object cfgs = activeDynamicPermissionsJson.get("cfgs");
    //         if (cfgs instanceof JSONArray) {
    //             final JSONArray jsonArray = (JSONArray) cfgs;
    //             for (Object object : jsonArray) {
    //                 addDynamicPermission(req, (JSONObject) object);
    //             }
    //         } else {
    //             // there might be only one single dynamic permission
    //             addDynamicPermission(req, (JSONObject) cfgs);
    //         }

    //     } else {
    //         activeDynamicPermissions = false;
    //         extractPattern = null;
    //         dynamicPermissionConfigs.clear();
    //     }

    //     save();
    //     result = true;

    //     return result;
    // }

    /**
     * adds a dynamic permission configuration with the data extracted from the
     * jsonObject.
     *
     * @param req
     * @param jsonObject
     */
    private void addDynamicPermission(StaplerRequest2 req, JSONObject jsonObject) {
        final DynamicPermissionConfig dynPerm = req.bindJSON(DynamicPermissionConfig.class, jsonObject);

        // add the enabled permission ids
        final Map<String, List<Permission>> allPossiblePermissions = getAllPossiblePermissions();
        for (Map.Entry<String, List<Permission>> entry : allPossiblePermissions.entrySet()) {
            for (Permission permission : entry.getValue()) {
                final String enabled = jsonObject.getString(permission.getId());
                if (Boolean.parseBoolean(enabled)) {
                    dynPerm.addPermissionId(permission.getId());
                    log.log(Level.FINE, "enable {0}", new String[] {permission.getId()});
                }
            }
        }

        dynamicPermissionConfigs.add(dynPerm);
    }

    /**
     *
     * @return
     */
    public static Map<String, List<Permission>> getAllPossiblePermissions() {
        final Map<String, List<Permission>> enabledPerms = new TreeMap<>();

        addEnabledPermissionsForGroup(enabledPerms, hudson.model.Item.class);
        addEnabledPermissionsForGroup(enabledPerms, hudson.model.Run.class);

        return enabledPerms;
    }

    /**
     *
     * @param p
     * @return
     */
    public static String impliedByList(@Nullable Permission p) {
        List<Permission> impliedBys = new ArrayList<>();
        while (null != p && null != p.impliedBy) {
            p = p.impliedBy;
            impliedBys.add(p);
        }
        return StringUtils.join(impliedBys.stream().map(Permission::getId).collect(Collectors.toList()), " ");
    }

    /**
     *
     * @param allEnabledPerms
     * @param owner
     */
    private static void addEnabledPermissionsForGroup(
            final Map<String, List<Permission>> allEnabledPerms, Class<?> owner) {
        final PermissionGroup permissionGroup = PermissionGroup.get(owner);
        if (permissionGroup != null) {
            final List<Permission> enabledPerms = new ArrayList<>();
            List<Permission> permissions = permissionGroup.getPermissions();
            for (Permission permission : permissions) {
                if (permission.enabled) {
                    enabledPerms.add(permission);
                }
            }
            if (!enabledPerms.isEmpty()) {
                allEnabledPerms.put(permissionGroup.title.toString(), enabledPerms);
            }
        }
    }

    /**
     *
     * @return true when automatic owner right assigment  option is activated
     */
    public boolean isAutoOwnerRights() {
        return autoOwnerRights;
    }

    @DataBoundSetter
    public void setAutoOwnerRights(boolean autoOwnerRights) {
        this.autoOwnerRights = autoOwnerRights;
        save();
    }

    /**
     *
     * @return true when automatic public browse assigment option is activated
     */
    public boolean isAutoPublicBrowse() {
        return autoPublicBrowse;
    }

    @DataBoundSetter
    public void setAutoPublicBrowse(boolean autoPublicBrowse) {
        this.autoPublicBrowse = autoPublicBrowse;
        save();
    }

    /**
     *
     * @return true when replace space option is activated
     */
    public boolean isReplaceSpace() {
        return replaceSpace;
    }

    @DataBoundSetter
    public void setReplaceSpace(boolean replaceSpace) {
        this.replaceSpace = replaceSpace;
        save();
    }

    /**
     *
     * @return true when log rotator option is activated
     */
    public boolean isActiveLogRotator() {
        //this.activeLogRotator = (getDaysToKeep() !=-1 || getNumToKeep() != -1 || getArtifactDaysToKeep()!=-1 || getArtifactNumToKeep() !=-1);
        return this.activeLogRotator;
    }

    @DataBoundSetter
    public void setActiveLogRotator(boolean activeLogRotator) {
        this.activeLogRotator = activeLogRotator;
        if(!activeLogRotator) {
            setNumToKeep(-1);
            setDaysToKeep(-1);
            setArtifactNumToKeep(-1);
            setArtifactDaysToKeep(-1);
        }
        save();
    }

    /**
     *
     * @return the days to keep builds
     */
    public int getDaysToKeep() {
        return daysToKeep;
    }

    @DataBoundSetter
    public void setDaysToKeep(int daysToKeep) {
        this.daysToKeep = daysToKeep;
        save();
    }

    /**
     *
     * @return the number of build to be kept
     */
    public int getNumToKeep() {
        return numToKeep;
    }

    @DataBoundSetter
    public void setNumToKeep(int numToKeep) {
        this.numToKeep = numToKeep;
        save();
    }

    /**
     *
     * @return the days to keep build artifacts
     */
    public int getArtifactDaysToKeep() {
        return artifactDaysToKeep;
    }

    @DataBoundSetter
    public void setArtifactDaysToKeep(int artifactDaysToKeep) {
        this.artifactDaysToKeep = artifactDaysToKeep;
        save();
    }

    /**
     *
     * @return the number of build to keep with artifacts
     */
    public int getArtifactNumToKeep() {
        return artifactNumToKeep;
    }

    @DataBoundSetter
    public void setArtifactNumToKeep(int artifactNumToKeep) {
        this.artifactNumToKeep = artifactNumToKeep;
        save();
    }

    /**
     * @return the extractPattern
     */
    public String getExtractPattern() {
        return extractPattern;
    }

    @DataBoundSetter
    public void setExtractPattern(String extractPattern) {
        this.extractPattern = extractPattern;
        save();
    }

    /**
     * @return the activeDynamicPermissions
     */
    public boolean isActiveDynamicPermissions() {
        return activeDynamicPermissions;
    }

    @DataBoundSetter
    public void setActiveDynamicPermissions(boolean activeDynamicPermissions) {
        this.activeDynamicPermissions = activeDynamicPermissions;
        save();
    }

    /**
     *
     * @return
     */
    public boolean isMvnArchivingDisabled() {
        return mvnArchivingDisabled;
    }

    @DataBoundSetter
    public void setMvnArchivingDisabled(boolean mvnArchivingDisabled) {
        this.mvnArchivingDisabled = mvnArchivingDisabled;
        save();
    }

    /**
     *
     * @return
     */
    public boolean isMvnPerModuleEmail() {
        return mvnPerModuleEmail;
    }

    @DataBoundSetter
    public void setMvnPerModuleEmail(boolean mvnPerModuleEmail) {
        this.mvnPerModuleEmail = mvnPerModuleEmail;
        save();
    }

    /**
     * @return the dynamicPermissionConfigs
     */
    public List<DynamicPermissionConfig> getDynamicPermissionConfigs() {
        return dynamicPermissionConfigs;
    }

    public void setDynamicPermissionConfigs(List<DynamicPermissionConfig> dynamicPermissionConfigs) {
        this.dynamicPermissionConfigs = dynamicPermissionConfigs;
        save();
    }

}
