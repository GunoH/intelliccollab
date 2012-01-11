package com.quinity.ccollab.intellij.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.quinity.ccollab.intellij.IntelliCcollabApplicationComponent;
import com.quinity.ccollab.intellij.MessageResources;
import com.smartbear.CollabClientException;
import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.IGlobalOptions;
import com.smartbear.beans.IScmOptions;
import com.smartbear.collections.Pair;
import org.apache.commons.lang.StringUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class IntelliCcollabConfigurationForm {
    private JPanel rootComponent;
    private JTextField urlField;
    private JTextField proxyPortField;
    private JTextField proxyHostField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton autofillButton;

    private static Logger logger = Logger.getInstance(IntelliCcollabConfigurationForm.class.getName());

    public IntelliCcollabConfigurationForm() {
        autofillButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    autofill();
                } catch (Exception ex) {
                    logger.error("Exception when reading metadata from filesystem. ", ex);
                    Messages.showErrorDialog(MessageResources.message("errorDialog.couldNotReadMetadata.text"),
                            MessageResources.message("errorDialog.couldNotReadMetadata.title"));
                }
            }
        });
    }

    /**
     * Fills the preferences with the values retrieved from the filesystem metadata.
     */
    private void autofill() throws IOException, CollabClientException {
        Pair<IGlobalOptions, IScmOptions> configOptions = ConfigUtils.loadConfigFiles();
        IGlobalOptions options = configOptions.getA();

        urlField.setText(options.getUrl().toString());
        proxyHostField.setText(options.getServerProxyHost());
        proxyPortField.setText(options.getServerProxyPort());
        usernameField.setText(options.getUser());
        passwordField.setText(options.getPassword());
    }


    /**
     * Method return root component of form.
     */
    public JComponent getRootComponent() {
        return rootComponent;
    }

    public void setData(IntelliCcollabApplicationComponent data) {
        urlField.setText(data.getServerURL());
        proxyHostField.setText(data.getServerProxyHost());
        proxyPortField.setText(data.getServerProxyPort());
        usernameField.setText(data.getUsername());
        passwordField.setText(data.getPassword());
    }

    public void getData(IntelliCcollabApplicationComponent data) throws MalformedURLException {
        String urlText = urlField.getText();
        if (StringUtils.isNotEmpty(urlText)) {
            // Validate the URL.
            new URL(urlText);
            data.setServerURL(urlText);
        } else {
            data.setServerURL(null);
        }

        data.setServerProxyHost(proxyHostField.getText());
        data.setServerProxyPort(proxyPortField.getText());
        data.setUsername(usernameField.getText());
        data.setPassword(String.valueOf(passwordField.getPassword()));
    }

    public boolean isModified(IntelliCcollabApplicationComponent data) {

        if (data.getServerURL() == null) {
            return urlField.getText() != null;
        }
        if (data.getServerProxyHost() == null) {
            return proxyHostField.getText() != null;
        }
        if (data.getServerProxyPort() == null) {
            return proxyPortField.getText() != null;
        }
        if (data.getUsername() == null) {
            return usernameField.getText() != null;
        }
        if (data.getPassword() == null) {
            return passwordField.getPassword() != null;
        }

        if (urlField.getText() == null) {
            return false;
        }
        if (proxyHostField.getText() == null) {
            return false;
        }
        if (proxyPortField.getText() == null) {
            return false;
        }
        if (usernameField.getText() == null) {
            return false;
        }
        if (passwordField.getPassword() == null) {
            return false;
        }

        if (!urlField.getText().equals(data.getServerURL())) {
            return true;
        }
        if (!proxyHostField.getText().equals(data.getServerProxyHost())) {
            return true;
        }
        if (!proxyPortField.getText().equals(data.getServerProxyPort())) {
            return true;
        }
        if (!usernameField.getText().equals(data.getUsername())) {
            return true;
        }
        return !Arrays.equals(passwordField.getPassword(), data.getPassword().toCharArray());
    }
}
