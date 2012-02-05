package nl.guno.ccollab.intellij;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.smartbear.CollabClientException;
import com.smartbear.ccollab.datamodel.Changelist;
import com.smartbear.ccollab.datamodel.Engine;
import com.smartbear.ccollab.datamodel.Review;
import com.smartbear.ccollab.datamodel.Scm;
import com.smartbear.ccollab.datamodel.User;
import com.smartbear.scm.IScmClientConfiguration;
import com.smartbear.scm.IScmLocalCheckout;
import com.smartbear.scm.ScmChangeset;
import com.smartbear.scm.ScmConfigurationException;
import com.smartbear.scm.ScmUtils;
import org.jetbrains.annotations.NotNull;

public class AddToReviewTask extends Task.Backgroundable {

    private static Logger logger = Logger.getInstance(AddToReviewTask.class.getName());

    private Project project;
    private Review review;

    private File[] files;

    private User user;

    private boolean success;
    private String errorMessage;

    public AddToReviewTask(Project project, Review review, User user, File... files) {
        super(project, MessageResources.message("task.addFilesToReview.title"), true);

        this.project = project;
        this.review = review;
        this.user = user;
        this.files = files;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {

        try {
            progressIndicator.setText(MessageResources.message("progressIndicator.addToReview.preparing"));

            // Create the SCM ChangeSet object to upload.  You can attach
            // many types of objects here from uncontrolled files as in this
            // example to controlled files (both local and server-side-only)
            // to SCM-specific atomic changelists (e.g. with Perforce and Subversion).
            logger.debug("Creating SCM Changeset...");
            ScmChangeset changeset = new ScmChangeset();


            IScmClientConfiguration clientConfig;
            IScmLocalCheckout scmFile = null;


            if (files.length == 0) {
                success = true;
                return;
            }

            int fileCounter = 0;
            for (File file : files) {

                progressIndicator.checkCanceled();

                progressIndicator.setText2(MessageResources.message("progressIndicator.addToReview.fileUploadProgress", file.getName(),
                        ++fileCounter, files.length));
                logger.debug("Working with file: " + file.getPath());


                if (file.isDirectory()) {
                    logger.warn("error: path points to a directory instead of to a file: " + file.getPath());
                    throw new IntelliCcollabException("error: path points to a directory instead of to a file: "
                            + file.getPath());
                }

                // Create the SCM object representing a local file under version control.
                // We assume the local SCM is already configured properly.
                logger.debug("Loading SCM File object...");
                clientConfig = ScmUtils.requireScm(file, AddControlledFileAction.scmOptions, new NullProgressMonitor(), ScmUtils.SCMS);
                scmFile = clientConfig.getLocalCheckout(file, new NullProgressMonitor());
                changeset.addLocalCheckout(scmFile, true, new NullProgressMonitor());
            }

            progressIndicator.checkCanceled();

            progressIndicator.setText(MessageResources.message("progressIndicator.addToReview.uploading"));
            progressIndicator.setText2("");

            // Upload this changeset to Collaborator.  Another form of this
            // uploader lets us specify even more information; this form extracts it
            // automatically from the files in the changeset.
            logger.debug("Uploading SCM Changeset...");
            Engine engine = AddControlledFileAction.engine;
            Scm scm = engine.scmByLocalCheckout(scmFile);            // select the SCM system that matches the client configuration

            Changelist changelist = scm.uploadChangeset(changeset, null, null, null, user.getLogin(),
                    "Local changes uploaded from IntelliJ IDEA", new NullProgressMonitor());

            progressIndicator.setText(MessageResources.message("progressIndicator.addToReview.attaching", review.getId()));

            progressIndicator.checkCanceled();

            // The changelist has been uploaded but it hasn't been attached
            // to any particular review!  This two-step process not only allows for
            // a changelist to be part of more than one review, but also means that
            // if there's any error in uploading the changelist the review hasn't
            // changed at all so no one will be affected.
            review.addChangelist(changelist, user);

            success = true;
        } catch (ScmConfigurationException e) {
            logger.warn(e);
            errorMessage = MessageResources.message("task.addFilesToReview.cannotDetermineSCMSystem.text");
        } catch (CollabClientException e) {
            logger.warn(e);
            errorMessage = MessageResources.message("task.addFilesToReview.errorOccurred.text");
        } catch (IntelliCcollabException e) {
            logger.warn(e);
            errorMessage = MessageResources.message("task.addFilesToReview.errorOccurred.error.text", e.getMessage());
        } catch (IOException e) {
            logger.warn(e);
            errorMessage = MessageResources.message("task.addFilesToReview.ioErrorOccurred.text");
        }

    }

    @Override
    public void onSuccess() {
        if (success) {
            PluginUtil.createBalloon(project, MessageResources.message("task.addFilesToReview.filesHaveBeenUploaded.text",
                    files.length, review.getId(), review.getTitle()), MessageType.INFO);
        } else {
            PluginUtil.createBalloon(project, errorMessage, MessageType.ERROR);
        }
    }

    @Override
    public void onCancel() {
        if (success) {
            PluginUtil.createBalloon(project, MessageResources.message("task.addFilesToReview.filesHaveBeenUploaded.text",
                    files.length, review.getId(), review.getTitle()),
                    MessageType.INFO);
        } else {
            PluginUtil.createBalloon(project, MessageResources.message("task.addFilesToReview.cancelled.text"), 
                    MessageType.ERROR);
        }
    }

    @Override
    public boolean shouldStartInBackground() {
        return false;
    }
}
