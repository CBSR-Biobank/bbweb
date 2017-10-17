/**
 * Angular module for common functionality.
 * @namespace common
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import AnnotationsInputModule from './annotationsInput/annotationsInputModule';
import ModalInputModule       from './modalInput/modalInputModule';
import angular                from 'angular';

const CommonModule = angular.module('biobank.common', [ ModalInputModule, AnnotationsInputModule ])
      .controller('TabbedPageController',          require('./controllers/TabbedPageController'))
      .controller('NameAndStateFiltersController', require('./controllers/NameAndStateFiltersController'))

      .component('breadcrumbs',             require('./components/breadcrumbs/breadcrumbsComponent').default)
      .component('collapsiblePanel',        require('./components/collapsiblePanel/collapsiblePanelComponent'))
      .component('dateTimePicker',          require('./components/dateTimePicker/dateTimePickerComponent'))
      .component('debouncedTextInput',      require('./components/debouncedTextInput/debouncedTextInputComponent'))
      .component('labelsInput',             require('./components/labelsInput/labelsInputComponent').default)
      .component('labelsList',              require('./components/labelsList/labelsListComponent'))
      .component('nameFilter',              require('./components/nameFilter/nameFilterComponent').default)
      .component('nameAndStateFilters',     require('./components/nameAndStateFilters/nameAndStateFiltersComponent').default)
      .component('nameEmailStateFilters',   require('./components/nameEmailStateFilters/nameEmailStateFiltersComponent').default)
      .component('progressTracker',         require('./components/progressTracker/progressTrackerComponent'))
      .component('statusLine',              require('./components/statusLine/statusLineComponent'))

      .directive('focusMe',                 require('./directives/focusMeDirective'))
      .directive('infoUpdateRemoveButtons', require('./directives/infoUpdateRemoveButtons/infoUpdateRemoveButtonsDirective'))
      .directive('integer',                 require('./directives/integer/integerDirective'))
      .directive('panelButtons',            require('./directives/panelButtons/panelButtonsDirective'))
      .directive('smartFloat',              require('./directives/smartFloat/smartFloatDirective'))
      .directive('str2integer',             require('./directives/str2integer/str2integerDirective'))
      .directive('updateRemoveButtons',     require('./directives/updateRemoveButtons/updateRemoveButtonsDirective'))
      .directive('positiveFloat',           require('./directives/positiveFloat/positiveFloatDirective'))
      .directive('naturalNumber',           require('./directives/naturalNumber/naturalNumberDirective'))
      .directive('truncateToggle',          require('./directives/truncateToggle/truncateToggleDirective'))

      .filter('localTime',                  require('./filters/localTimeFilter'))
      .filter('nl2br',                      require('./filters/nl2brFilter'))
      .filter('timeago',                    require('./filters/timeagoFilter').default)
      .filter('truncate',                   require('./filters/truncateFilter'))
      .filter('yesNo',                      require('./filters/yesNoFilter'))

      .service('asyncInputModal',           require('./services/asyncInputModal/asyncInputModalService'))
      .service('breadcrumbService',         require('./services/breadcrumbService'))
      .service('Panel',                     require('./services/Panel'))
      .service('annotationUpdate',          require('./services/annotationUpdateService'))
      .service('biobankApi',                require('./services/biobankApi/biobankApiService'))
      .service('filterExpression',          require('./services/filterExpression/filterExpressionService'))
      .service('funutils',                  require('./services/funutils'))
      .service('modalService',              require('./services/modalService/modalService'))
      .service('notificationsService',      require('./services/notifications/notificationsService'))
      .service('timeService',               require('./services/time/timeService'))
      .service('validationService',         require('./services/validationService'))
      .service('domainNotificationService', require('./services/domainNotification/domainNotificationService') )
      .service('languageService',           require('./services/language/languageService').default)
      .service('labelService',              require('./services/labelService'))
      .service('UrlService',                require('./services/url/UrlService').default)

      .factory('BbwebError',                require('./BbwebError'))
      .run(loadTemplates)
      .name;

/* @ngInject */
function loadTemplates($templateCache) {
  $templateCache.put('smartTablePaginationTemplate.html', require('./smartTablePaginationTemplate.html'));
}

export default CommonModule;
