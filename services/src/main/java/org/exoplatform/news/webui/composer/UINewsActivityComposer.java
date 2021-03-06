package org.exoplatform.news.webui.composer;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.webui.activity.UINewsActivity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.PeopleService;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIActivityComposer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;

@ComponentConfig(
        template = "war:/groovy/news/webui/composer/UINewsActivityComposer.gtmpl",
        events = {
                @EventConfig(listeners = UIActivityComposer.CloseActionListener.class),
                @EventConfig(listeners = UIActivityComposer.SubmitContentActionListener.class),
                @EventConfig(listeners = UIActivityComposer.ActivateActionListener.class)
        }
)

public class UINewsActivityComposer extends UIActivityComposer {

  private static final Log LOG = ExoLogger.getLogger(UINewsActivityComposer.class);

  private static final String NEWS_FEATURE_NAME = "news";

  private static final String REDACTOR_MEMBERSHIP_NAME = "redactor";

  private static final String MANAGER_MEMBERSHIP_NAME = "manager";
  
  private final static String PUBLISHER_MEMBERSHIP_NAME = "publisher";
  
  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private ExoFeatureService featureService;

  public UINewsActivityComposer() {
    featureService = CommonsUtils.getService(ExoFeatureService.class);
  }

  @Override
  public boolean isEnabled() {
    if (!featureService.isActiveFeature(NEWS_FEATURE_NAME)) {
      return false;
    }

    Space space = getCurrentSpace();

    if (space == null) {
      return false;
    }

    org.exoplatform.services.security.Identity ownerIdentity = ConversationState.getCurrent().getIdentity();
    return ownerIdentity.isMemberOf(space.getGroupId(), REDACTOR_MEMBERSHIP_NAME)
            || ownerIdentity.isMemberOf(space.getGroupId(), MANAGER_MEMBERSHIP_NAME)
            || ownerIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }

  @Override
  protected void onPostActivity(UIComposer.PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception {

  }

  @Override
  protected void onClose(Event<UIActivityComposer> event) {

  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> event) {

  }

  @Override
  protected void onActivate(Event<UIActivityComposer> event) {
  }

  private Space getCurrentSpace() {
    Space space = null;
    UIContainer uiContainer = getActivityDisplay();
    if (uiContainer instanceof UISpaceActivitiesDisplay) {
      space = ((UISpaceActivitiesDisplay) uiContainer).getSpace();
    }
    return space;
  }

}
