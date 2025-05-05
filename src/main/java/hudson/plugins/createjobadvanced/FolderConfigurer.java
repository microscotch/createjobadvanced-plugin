package hudson.plugins.createjobadvanced;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.jenkinsci.plugins.matrixauth.PermissionEntry;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;

import hudson.model.AbstractItem;
import hudson.security.Permission;
import jenkins.model.Jenkins;

public class FolderConfigurer {

    public FolderConfigurer() {}

    protected void renameJob(final AbstractItem folder) throws IOException {
        if(folder instanceof AbstractFolder<?>) {
            ((AbstractFolder<?>)folder).renameTo(folder.getName().replaceAll(" ", "-"));
        }
        
    }

    protected void addAuthorizationMatrixProperty(
            AbstractFolder<?> folder, Map<Permission, Set<PermissionEntry>> permissions) throws IOException {
        Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            log.warning("Jenkins instance is null");
            return;
        }

        com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty.DescriptorImpl propDescriptor =
                (com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty.DescriptorImpl)
                        instance.getDescriptor(
                                com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty.class);

        if (propDescriptor == null) {
            log.warning("AuthorizationMatrixProperty.DescriptorImpl is null");
            return;
        }

        com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty authProperty =
                propDescriptor.create();
        for (Map.Entry<Permission, Set<PermissionEntry>> entry : permissions.entrySet()) {
            Permission perm = entry.getKey();
            for (PermissionEntry permEntry : entry.getValue()) {
                authProperty.add(perm, permEntry);
            }
        }
        folder.addProperty(authProperty);
    }

}

    