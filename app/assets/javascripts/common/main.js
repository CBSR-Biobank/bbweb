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

    .controller('TabbedPageController',          require('./controllers/TabbedPageController'))
    .controller('PagedListController',           require('./controllers/PagedListController'))
    .controller('NameAndStateFiltersController', require('./controllers/NameAndStateFiltersController'))

    .component('breadcrumbs',             require('./components/breadcrumbs/breadcrumbsComponent'))
    .component('collapsiblePanel',        require('./components/collapsiblePanel/collapsiblePanelComponent'))
    .component('dateTimePicker',          require('./components/dateTimePicker/dateTimePickerComponent'))
    .component('debouncedTextInput',      require('./components/debouncedTextInput/debouncedTextInputComponent'))
    .component('labelsInput',             require('./components/labelsInput/labelsInputComponent'))
    .component('nameFilter',              require('./components/nameFilter/nameFilterComponent'))
    .component('nameAndStateFilters',     require('./components/nameAndStateFilters/nameAndStateFiltersComponent'))
    .component('nameEmailStateFilters',   require('./components/nameEmailStateFilters/nameEmailStateFiltersComponent'))
    .component('progressTracker',         require('./components/progressTracker/progressTrackerComponent'))
    .component('statusLine',              require('./components/statusLine/statusLineComponent'))

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
    .directive('truncateToggle',          require('./directives/truncateToggle/truncateToggleDirective'))

    .filter('localTime',                  require('./filters/localTimeFilter'))
    .filter('nl2br',                      require('./filters/nl2brFilter'))
    .filter('timeago',                    require('./filters/timeagoFilter'))
    .filter('truncate',                   require('./filters/truncateFilter'))
    .filter('yesNo',                      require('./filters/yesNoFilter'))

    .service('breadcrumbService',         require('./services/breadcrumbService'))
    .service('Panel',                     require('./services/Panel'))
    .service('annotationUpdate',          require('./services/annotationUpdateService'))
    .service('biobankApi',                require('./services/biobankApiService'))
    .service('filterExpression',          require('./services/filterExpressionService'))
    .service('funutils',                  require('./services/funutils'))
    .service('modalService',              require('./services/modalService/modalService'))
    .service('notificationsService',      require('./services/notificationsService'))
    .service('timeService',               require('./services/timeService'))
    .service('validationService',         require('./services/validationService'))
    .service('domainNotificationService', require('./services/domainNotification/domainNotificationService') )
    .service('languageService',           require('./services/languageService'))
    .service('labelService',              require('./services/labelService'))

    .factory('BbwebError',                require('./BbwebError'));

  return {
    name: name,
    module: module
  };
});
