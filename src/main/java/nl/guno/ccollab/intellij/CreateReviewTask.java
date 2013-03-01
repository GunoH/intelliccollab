package nl.guno.ccollab.intellij;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.smartbear.ccollab.datamodel.GroupDescription;
import com.smartbear.ccollab.datamodel.IDropDownItem;
import com.smartbear.ccollab.datamodel.MetaDataDescription;
import com.smartbear.ccollab.datamodel.MetaDataSelectItem;
import com.smartbear.ccollab.datamodel.Review;
import com.smartbear.ccollab.datamodel.ReviewAccess;
import com.smartbear.ccollab.datamodel.ReviewParticipant;
import com.smartbear.ccollab.datamodel.Role;
import com.smartbear.ccollab.datamodel.User;
import nl.guno.ccollab.intellij.ui.Notification;

public class CreateReviewTask extends Task.Modal {

    private static Logger logger = Logger.getInstance(CreateReviewTask.class.getName());

    private boolean success;

    private Project project;
    private User user;
    private GroupDescription group;
    private String reviewTitle;
    private boolean uploadRestricted;
    private ReviewAccess reviewAccess;
    private User author;
    private User reviewer;
    private User observer;
    private Map<MetaDataDescription, Object> metadata;

    private Review review;

    private IntelliCcollabApplicationComponent component =
            ApplicationManager.getApplication().getComponent(IntelliCcollabApplicationComponent.class);

    public CreateReviewTask(Project project, User user, GroupDescription group, String reviewTitle,
                            boolean uploadRestricted, ReviewAccess reviewAccess, User author, User reviewer,
                            User observer, Map<MetaDataDescription, Object> metadata) {
        super(project, MessageResources.message("task.createReview.title"), false);

        this.project = project;
        this.user = user;
        this.group = group;
        this.reviewTitle = reviewTitle;
        this.uploadRestricted = uploadRestricted;
        this.reviewAccess = reviewAccess;
        this.author = author;
        this.reviewer = reviewer;
        this.observer = observer;
        this.metadata = metadata;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {

        progressIndicator.setText(MessageResources.message("progressIndicator.createReview.start"));

        createReview();

        success = true;
    }

    /**
     * Creates a new review object
     */
    private void createReview() {

        // Create the new review object with the local user as the creator
        review = user.getEngine().reviewCreate(user, reviewTitle);
        review.setGroup(group);
        review.setUploadRestricted(uploadRestricted);
        review.setReviewAccess(reviewAccess);

        String template = user.getEngine().roleStandardTemplateName();
        Role[] roles = user.getEngine().rolesFind(template, true);

        if (author != null) {
            review.addParticipants(new ReviewParticipant(author, Role.findAuthor(roles)));
        }
        if (reviewer != null) {
            review.addParticipants(new ReviewParticipant(reviewer, Role.findReviewer(roles)));
        }
        if (observer != null) {
            review.addParticipants(new ReviewParticipant(observer, Role.findObserver(roles)));
        }

        if (metadata != null) {
            for (Map.Entry<MetaDataDescription, Object> entry : metadata.entrySet()) {
                switch (entry.getKey().getType()) {
                    case CHARACTER:
                        review.getUserDefinedFields().setCharacter(entry.getKey().getTitle(), (Character) entry.getValue());
                        break;
                    case INTEGER:
                        review.getUserDefinedFields().setInteger(entry.getKey().getTitle(), (Integer) entry.getValue());
                        break;
                    case INTEGER_SET:
                        review.getUserDefinedFields().setIntegerSet(entry.getKey().getTitle(), (Set<Integer>) entry.getValue());
                        break;
                    case MULTI_SELECTION:
                        review.getUserDefinedFields().setMultiSelectItems(entry.getKey().getTitle(), (Collection<IDropDownItem>) entry.getValue());
                        break;
                    case SELECTION:
                        review.getUserDefinedFields().setSelectItem(entry.getKey().getTitle(), (MetaDataSelectItem) entry.getValue());
                        break;
                    case STRING:
                    case STRINGBIG:
                        review.getUserDefinedFields().setString(entry.getKey().getTitle(), (String) entry.getValue());
                    break;
                }
            }
        }
        review.save();
        logger.debug("New review created: " + review.getDisplayText(true));
    }

    @Override
    public void onSuccess() {
        if (success) {
            new Notification(
                    project,
                    MessageResources.message("task.createReview.reviewCreated.text", review.getId(), review.getTitle(), component.getRegularServerURL()),
                    MessageType.INFO).showBalloon().addToEventLog();
            
        } else {
            new Notification(
                    project,
                    MessageResources.message("task.createReview.errorOccurred.text"),
                    MessageType.ERROR).showBalloon();
        }
    }
}