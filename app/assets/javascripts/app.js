/**
 * The app module as an AngularJS module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global PRODUCTION, DEVELOPMENT */

import 'bootstrap/dist/css/bootstrap.css';
import 'angular-toastr/dist/angular-toastr.min.css';
import '../stylesheets/main.less';

// app modules
// import AdminModule             from './admin';
// import AppConfig               from './AppConfig';
// import CentresModule           from './centres';
// import CollectionModule        from './collection';
// import DomainModule            from './domain';
// import HomeModule              from './home';
// import ShipmentSpecimensModule from './shipmentSpecimens';
// import StudiesModule           from './studies';
// import UsersModule             from './users';

// node modules
import angular           from 'angular';
import angularAnimate    from 'angular-animate';
import angularCookies    from 'angular-cookies';
import angularGettext    from 'angular-gettext';
import angularMessages   from 'angular-messages';
import angularSanitize   from 'angular-sanitize';
import angularSmartTable from 'angular-smart-table';
import angularToastr     from 'angular-toastr';
import angularUiRouter   from '@uirouter/angularjs';
import uiBootstrap       from 'angular-ui-bootstrap';
import uiDatetimePicker  from 'bootstrap-ui-datetime-picker';

// app modules
const loadModules = require.context('./', true, /^\.[\\\/][a-zA-Z]+[\\\/]index\.js$/)
const moduleNames = []

loadModules.keys().forEach((key) => {
  moduleNames.push(loadModules(key).default)
})

const appModule = angular.module('biobankApp',
                                 [
                                   // node modules
                                   angularAnimate,
                                   angularCookies,
                                   angularGettext,
                                   angularMessages,
                                   angularSanitize,
                                   angularSmartTable,
                                   angularToastr,
                                   angularUiRouter,
                                   uiBootstrap,
                                   uiDatetimePicker
                                 ].concat(moduleNames))
      .run(['languageService', function (languageService) {
        languageService.initLanguage({ debug: true });
      }])

      .run(uiRouterTrace) // For tracing state transitions
      .run(uiRouterIsAuthorized) // For authorization checks
      .config(loggingConfig)
      .config(configToastr);

// top level AngularJS elements
const context = require.context('./', false, /^((?!(app|test)).)*\.js$/)

context.keys().forEach(key => {
  context(key).default(appModule)
})

export default appModule.name;

/* @ngInject */
function configToastr(toastrConfig) {
  angular.extend(toastrConfig, {
    positionClass: 'toast-bottom-right'
  });

}

/* @ngInject */
function uiRouterTrace($trace) {
  if (DEVELOPMENT) {
    $trace.enable('TRANSITION');
  }
}

/* @ngInject */
function uiRouterIsAuthorized($transitions) {
  $transitions.onStart({ to: 'home.admin.**' },      checkIsAuthenticated);
  $transitions.onStart({ to: 'home.collection.**' }, checkIsAuthenticated);
  $transitions.onStart({ to: 'home.shipping.**' },   checkIsAuthenticated);

  function checkIsAuthenticated(transition) {
    var usersService = transition.injector().get('usersService'),
        $log = transition.injector().get('$log');
    return usersService.requestCurrentUser()
      .catch(function (error) {
        // User isn't authenticated. Redirect to a new Target State
        if (error.status && (error.status === 401)) {
          return transition.router.stateService.target('home.users.login');
        }
        $log.error(error);
        return null;
      });
  }
}

/* @ngInject */
function loggingConfig($logProvider) {
  if (!PRODUCTION) {
    $logProvider.debugEnabled(true);
  }
}
