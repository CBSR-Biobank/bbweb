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

// app modules
import AdminModule             from './admin';
import AppConfig               from './AppConfig';
import CentresModule           from './centres';
import CollectionModule        from './collection';
import DomainModule            from './domain';
import HomeModule              from './home';
import ShipmentSpecimensModule from './shipmentSpecimens';
import StudiesModule           from './studies';
import UsersModule             from './users';

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

const biobankApp = angular.module('biobankApp',
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
                                    AdminModule,
                                    CentresModule,
                                    CollectionModule,
                                    DomainModule,
                                    HomeModule,
                                    ShipmentSpecimensModule,
                                    StudiesModule,
                                    UsersModule
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
      .config(configToastr)
      .name;

export default biobankApp;

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

  httpInterceptor.$inject = ['$q', '$injector'];
  function httpInterceptor($q, $injector) {
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
