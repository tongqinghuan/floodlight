package com.react.verify;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.routing.Router;

public class VerifyWebRoutetable implements RestletRoutable {
        /**
         * Create the Restlet router and bind to the proper resources.
         */
        @Override
        public Router getRestlet(Context context) {
            Router router = new Router(context);
            router.attach("/module/status/json",       VerifyStatusResource.class);
            router.attach("/module/enable/json",       VerifyEnableResource.class);
            router.attach("/module/disable/json",      VerifyDisableResource.class);
            router.attach("/module/storageRules/json", SemeticRulesResource.class);

            router.attach("/rules/json",               SemeticRulesResource.class);

            return router;
        }

        /**
         * Set the base path for the Firewall
         */
        @Override
        public String basePath() {
            return "/wm/verify";
        }
    }

