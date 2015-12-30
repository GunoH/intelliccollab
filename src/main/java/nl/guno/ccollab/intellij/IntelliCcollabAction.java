package nl.guno.ccollab.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.smartbear.CollabClientException;
import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.IGlobalOptions;
import com.smartbear.beans.IScmOptions;
import com.smartbear.ccollab.CommandLineClient;
import com.smartbear.ccollab.client.ICollabClientInterface;
import com.smartbear.ccollab.datamodel.Engine;
import com.smartbear.ccollab.datamodel.User;
import com.smartbear.collections.Pair;
import nl.guno.ccollab.intellij.ui.Notification;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;

abstract class IntelliCcollabAction extends AnAction {

    /**
     * SCM options, created by {@link #init(com.intellij.openapi.project.Project)}
     */
    static IScmOptions scmOptions;

    /**
     * Connection to Code Collaborator server
     * created by {@link #init(com.intellij.openapi.project.Project)}
     */
    static Engine engine;

    /**
     * Currently logged-in user
     * created by {@link #init(com.intellij.openapi.project.Project)}
     */
    static User user;

    private static final IntelliCcollabSettings component =
            ApplicationManager.getApplication().getComponent(IntelliCcollabSettings.class);

    static boolean init(final Project project) throws CollabClientException, IOException, InterruptedException {

	    if (!new Environment().checkConnection()) {
		    new Notification(project, MessageResources.message("action.error.serverNotAvailable.text"),
				    MessageType.ERROR).showBalloon().addToEventLog();
		    return false;
	    }

        // If we've already initialized, don't do it again.
        if ( engine != null ) {
            return true;
        }

        //load options from config files
        Pair<IGlobalOptions, IScmOptions> configOptions = ConfigUtils.loadConfigFiles();
        IntelliCcollabGlobalOptions globalOptions = new IntelliCcollabGlobalOptions(configOptions.getA());

        if (globalOptions.settingsIncomplete()) {
            new Notification(project, MessageResources.message("configuration.error.mandatorySettingsMissing.text"),
                    MessageType.ERROR).setHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        openSettings(project);
                    }
                }
            }).showBalloon().addToEventLog();
            return false;
        }

        scmOptions = configOptions.getB();

        //initialize client interface
        ICollabClientInterface clientInterface = new CommandLineClient(globalOptions);

        //connect to server and log in (throws exception if authentication fails, can't find server, etc...)
        LoginTask loginTask = new LoginTask(project, globalOptions, clientInterface);
        loginTask.queue();

        if (!loginTask.success()) {
            
            String errorMessage;
            if (loginTask.authenticationErrorOccured()) {
                errorMessage = MessageResources.message("task.login.authenticationError.text");
            } else {
                errorMessage = MessageResources.message("task.login.unknowError.text");
            }

            new Notification(project, errorMessage,
                    MessageType.ERROR).setHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        openSettings(project);
                    }
                }
            }).showBalloon().addToEventLog();


            return false;
        }
        
        user = loginTask.getUser();

        if (user != null) {
            engine = user.getEngine();
        }

	    return true;
    }

    private static void openSettings(Project project) {
        ShowSettingsUtil.getInstance().editConfigurable(project, component);
    }

    /**
     * Called to clean up a previous call to {@link #init(Project)}.
     * <p/>
     * <b>THIS IS CRITICAL</b>.  If you do not close out your {@code CollabClientConnection}
     * object, data might not be flushed out to the server!
     */
    void finished() {
        if (engine != null) {
            engine.close(true);
        }
    }

}
