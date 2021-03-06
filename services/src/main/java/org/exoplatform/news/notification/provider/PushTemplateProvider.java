package org.exoplatform.news.notification.provider;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.plugin.CommentNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.CommentSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.PostNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareMyNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareNewsNotificationPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PostNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = ShareNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = ShareMyNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeSharedNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = CommentNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = CommentSharedNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl") })
public class PushTemplateProvider extends WebTemplateProvider {
  protected static Log log = ExoLogger.getLogger(PushTemplateProvider.class);

  public PushTemplateProvider(InitParams initParams) {
    super(initParams);
  }
}
