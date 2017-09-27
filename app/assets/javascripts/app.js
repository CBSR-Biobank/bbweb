/**
 * The app module as an AngularJS module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import 'bootstrap/dist/css/bootstrap.css';
import 'angular-toastr/dist/angular-toastr.min.css';
import '../stylesheets/main.less';
import '../stylesheets/breadcrumbs.less';
import '../stylesheets/progress-tracker.less';
import '../stylesheets/labels-list.less';

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
import AppConfig         from './AppConfig';
import common            from './common';
import admin             from './admin';
import home              from './home';
import domain            from './domain';
import users             from './users';
import centres           from './centres';
import studies           from './studies';
import collection        from './collection';
import shipmentSpecimens from './shipmentSpecimens';

const MODULE_NAME = 'biobankApp';

// We must already declare most dependencies here (except for common), or the submodules' routes
// will not be resolved
angular
  .module(MODULE_NAME,
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
            uiDatetimePicker,

            // app modules
            admin,
            centres,
            collection,
            domain,
            home,
            shipmentSpecimens,
            studies,
            users
          ])

  .run(['languageService', function (languageService) {
    languageService.initLanguage({ debug: true });
  }])

  .run(uiRouterTrace) // For tracing state transitions
  .run(uiRouterIsAuthorized) // For authorization checks
  .provider('AppConfig', AppConfig)

// see http://blog.thoughtram.io/angularjs/2014/12/22/exploring-angular-1.3-disabling-debug-info.html
//
// app.config(['$compileProvider', function ($compileProvider) {
//   $compileProvider.debugInfoEnabled(false);
// }]);

//.config(exceptionConfig)
//.config(httpInterceptorConfig)
  .config(loggingConfig)
  .config(configToastr);


configToastr.$inject = ['toastrConfig'];
function configToastr(toastrConfig) {
  angular.extend(toastrConfig, {
    positionClass: 'toast-bottom-right'
  });

}

uiRouterTrace.$inject = ['$trace'];

function uiRouterTrace($trace) {
  $trace.enable('TRANSITION');
}

uiRouterIsAuthorized.$inject = ['$transitions'];

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

loggingConfig.$inject = ['$logProvider'];
function loggingConfig($logProvider) {
  $logProvider.debugEnabled(true);
}

httpInterceptorConfig.$inject = ['$httpProvider'];
function httpInterceptorConfig($httpProvider) {
  $httpProvider.interceptors.push(httpInterceptor);

  //---

  httpInterceptor.$inject = ['$q', '$timeout', '$injector'];
  function httpInterceptor($q, $timeout, $injector) {
    return { 'responseError': responseError };

    //--

    function responseError(rejection) {
      if (rejection.status === 401) {
        $injector.get('usersService').sessionTimeout();
        $injector.get('$state').go('home.users.login', {}, { reload: true });
        $injector.get('$log').info('your session has timed out');
      }
      return $q.reject(rejection);
    }
  }
}

export default MODULE_NAME;
