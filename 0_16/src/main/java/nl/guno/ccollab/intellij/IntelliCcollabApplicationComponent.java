package nl.guno.ccollab.intellij;

import java.net.MalformedURLException;

import javax.swing.*;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import nl.guno.ccollab.intellij.ui.IntelliCcollabConfigurationForm;

public class IntelliCcollabApplicationComponent implements ApplicationComponent, Configurable, JDOMExternalizable {
    private IntelliCcollabConfigurationForm form;
    
    /* The settings themselves; these need to be public for IntelliJ to save them. */
    
    public String httpServerURL;
    public String regularServerURL;
    public String serverProxyHost;
    public String serverProxyPort;
    public String username;
    public String password;

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "IntelliCcollabApplicationComponent";
    }

    public String getHttpServerURL() {
        return httpServerURL;
    }

    public void setHttpServerURL(final String httpServerURL) {
        this.httpServerURL = httpServerURL;
    }

    public String getRegularServerURL() {
        return regularServerURL;
    }

    public void setRegularServerURL(String regularServerURL) {
        this.regularServerURL = regularServerURL;
    }

    public String getServerProxyHost() {
        return serverProxyHost;
    }

    public void setServerProxyHost(String serverProxyHost) {
        this.serverProxyHost = serverProxyHost;
    }

    public String getServerProxyPort() {
        return serverProxyPort;
    }

    public void setServerProxyPort(String serverProxyPort) {
        this.serverProxyPort = serverProxyPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return "IntelliCcollab";
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (form == null) {
            form = new IntelliCcollabConfigurationForm();
        }
        return form.getRootComponent();
    }

    public boolean isModified() {
        return form != null && form.isModified(this);
    }

    public void apply() throws ConfigurationException {
        if (form != null) {
            try {
                form.getData(this);
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Invalid URL");
            }
        }
    }

    public void reset() {
        if (form != null) {
            form.setData(this);
        }
    }

    public void disposeUIResources() {
        form = null;
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}