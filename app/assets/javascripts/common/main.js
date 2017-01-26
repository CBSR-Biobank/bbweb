/**
 * Angular module for common functionality.
 * @namespace common
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                = require('angular'),
      annotationsInputModule = require('./annotationsInput/annotationsInputModule'),
      modalInputModule       = require('./modalInput/modalInputModule'),
      name                   = 'biobank.common',
      module;

  module = angular.module('biobank.common', [ modalInputModule.name, annotationsInputModule.name ])

    .controller('TabbedPageController',   require('./controllers/TabbedPageController'))
    .controller('PagedListController',    require('./controllers/PagedListController'))

    .component('dateTimePicker',          require('./components/dateTimePicker/dateTimePickerComponent'))
    .component('collapsablePanel',        require('./components/collapsablePanel/collapsablePanelComponent'))
    .component('nameAndStateFilters',     require('./components/nameAndStateFilters/nameAndStateFiltersComponent'))
    .component('progressTracker',         require('./components/progressTracker/progressTrackerComponent'))

    .directive('focusMe',                 require('./directives/focusMeDirective'))
    .directive('infoUpdateRemoveButtons', require('./directives/infoUpdateRemoveButtonsDirective'))
    .directive('integer',                 require('./directives/integerDirective'))
    .directive('panelButtons',            require('./directives/panelButtonsDirective'))
    .directive('smartFloat',              require('./directives/smartFloatDirective'))
    .directive('str2integer',             require('./directives/str2integerDirective'))
    .directive('updateRemoveButtons',     require('./directives/updateRemoveButtonsDirective'))
    .directive('pageSelect',              require('./directives/pageSelectDirective'))
    .directive('positiveFloat',           require('./directives/positiveFloat/positiveFloatDirective'))
    .directive('naturalNumber',           require('./directives/naturalNumber/naturalNumberDirective'))
    .directive('statusLine',              require('./directives/statusLine/statusLineDirective'))
    .directive('truncateToggle',          require('./directives/truncateToggle/truncateToggleDirective'))

    .filter('localTime',                  require('./filters/localTimeFilter'))
    .filter('nl2br',                      require('./filters/nl2brFilter'))
    .filter('timeago',                    require('./filters/timeagoFilter'))
    .filter('truncate',                   require('./filters/truncateFilter'))

    .service('Panel',                     require('./services/Panel'))
    .service('annotationUpdate',          require('./services/annotationUpdateService'))
    .service('biobankApi',                require('./services/biobankApiService'))
    .service('filterExpression',          require('./services/filterExpressionService'))
    .service('funutils',                  require('./services/funutils'))
    .service('modalService',              require('./services/modalService/modalService'))
    .service('notificationsService',      require('./services/notificationsService'))
    .service('stateHelper',               require('./services/stateHelperService'))
    .service('timeService',               require('./services/timeService'))
    .service('validationService',         require('./services/validationService'))
    .service('domainNotificationService', require('./services/domainNotification/domainNotificationService') )
    .service('languageService',           require('./services/languageService'))

    .factory('BbwebError',                require('./BbwebError'));

  return {
    name: name,
    module: module
  };
});
