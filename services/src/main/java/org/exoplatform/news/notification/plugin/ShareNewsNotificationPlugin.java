package org.exoplatform.news.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.spi.SpaceService;

public class ShareNewsNotificationPlugin extends PostNewsNotificationPlugin {
  private static final Log   LOG = ExoLogger.getLogger(ShareNewsNotificationPlugin.class);

  public final static String ID  = "ShareNewsNotificationPlugin";

  public ShareNewsNotificationPlugin(InitParams initParams, SpaceService spaceService, OrganizationService organizationService) {
    super(initParams, spaceService, organizationService);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }
}
