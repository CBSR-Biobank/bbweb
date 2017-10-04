/**
 * Angular module for common functionality.
 * @namespace common
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import AnnotationsInputModule from './annotationsInput/annotationsInputModule';
import ModalInputModule       from './modalInput/modalInputModule';
import UrlService             from './services/url/UrlService';
import angular                from 'angular';
import languageService        from './services/language/languageService';

const CommonModule = angular.module('biobank.common', [ ModalInputModule, AnnotationsInputModule ])
      .controller('TabbedPageController',          require('./controllers/TabbedPageController'))
      .controller('PagedListController',           require('./controllers/PagedListController'))
      .controller('NameAndStateFiltersController', require('./controllers/NameAndStateFiltersController'))

      .component('breadcrumbs',             require('./components/breadcrumbs/breadcrumbsComponent'))
      .component('collapsiblePanel',        require('./components/collapsiblePanel/collapsiblePanelComponent'))
      .component('dateTimePicker',          require('./components/dateTimePicker/dateTimePickerComponent'))
      .component('debouncedTextInput',      require('./components/debouncedTextInput/debouncedTextInputComponent'))
      .component('labelsInput',             require('./components/labelsInput/labelsInputComponent'))
      .component('labelsList',              require('./components/labelsList/labelsListComponent'))
      .component('nameFilter',              require('./components/nameFilter/nameFilterComponent'))
      .component('nameAndStateFilters',     require('./components/nameAndStateFilters/nameAndStateFiltersComponent'))
      .component('nameEmailStateFilters',   require('./components/nameEmailStateFilters/nameEmailStateFiltersComponent'))
      .component('progressTracker',         require('./components/progressTracker/progressTrackerComponent'))
      .component('statusLine',              require('./components/statusLine/statusLineComponent'))

      .directive('focusMe',                 require('./directives/focusMeDirective'))
      .directive('infoUpdateRemoveButtons', require('./directives/infoUpdateRemoveButtons/infoUpdateRemoveButtonsDirective'))
      .directive('integer',                 require('./directives/integer/integerDirective'))
      .directive('panelButtons',            require('./directives/panelButtons/panelButtonsDirective'))
      .directive('smartFloat',              require('./directives/smartFloat/smartFloatDirective'))
      .directive('str2integer',             require('./directives/str2integer/str2integerDirective'))
      .directive('updateRemoveButtons',     require('./directives/updateRemoveButtons/updateRemoveButtonsDirective'))
      .directive('pageSelect',              require('./directives/pageSelectDirective'))
      .directive('positiveFloat',           require('./directives/positiveFloat/positiveFloatDirective'))
      .directive('naturalNumber',           require('./directives/naturalNumber/naturalNumberDirective'))
      .directive('truncateToggle',          require('./directives/truncateToggle/truncateToggleDirective'))

      .filter('localTime',                  require('./filters/localTimeFilter'))
      .filter('nl2br',                      require('./filters/nl2brFilter'))
      .filter('timeago',                    require('./filters/timeagoFilter'))
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
      .service('languageService',           languageService)
      .service('labelService',              require('./services/labelService'))
      .service('UrlService',                UrlService)

      .factory('BbwebError',                require('./BbwebError'))
      .run(loadTemplates)
      .name;

loadTemplates.$inject = ['$templateCache'];
function loadTemplates($templateCache) {
  $templateCache.put('smartTablePaginationTemplate.html', require('./smartTablePaginationTemplate.html'));
}


export default CommonModule;
