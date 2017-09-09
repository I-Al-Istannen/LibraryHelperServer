package me.ialistannen.libraryhelperserver.server.authentication;

import java.util.List;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.profile.UndertowProfileManager;

/**
 * A {@link SecurityLogic} which errors are send in json.
 */
public class JsonSecurityLogic extends DefaultSecurityLogic<Object, UndertowWebContext> {

  public JsonSecurityLogic() {
    setProfileManagerFactory(UndertowProfileManager::new);
  }

  @Override
  protected HttpAction forbidden(UndertowWebContext context, List<Client> currentClients,
      List<CommonProfile> profiles, String authorizers) {

    HttpStatusSender.forbidden(context.getExchange(), "Forbidden");
    return new HttpAction("Forbidden", HttpConstants.FORBIDDEN);
  }

  @Override
  protected HttpAction unauthorized(UndertowWebContext context, List<Client> currentClients) {
    HttpStatusSender.unauthorized(context.getExchange(), "Unauthorized");
    return new HttpAction("Unauthorized", HttpConstants.UNAUTHORIZED);
  }
}
