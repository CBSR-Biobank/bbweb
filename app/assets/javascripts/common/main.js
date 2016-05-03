/**
 * Module for common functionality.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.common',
      module,
      annotationsInputModule = require('./annotationsInput/annotationsInputModule'),
      modalInputModule = require('./modalInput/modalInputModule');

  module = angular.module('biobank.common', [ modalInputModule.name, annotationsInputModule.name ]);

  module.directive('focusMe',                 require('./directives/focusMeDirective'));
  module.directive('infoUpdateRemoveButtons', require('./directives/infoUpdateRemoveButtonsDirective'));
  module.directive('integer',                 require('./directives/integerDirective'));
  module.directive('panelButtons',            require('./directives/panelButtonsDirective'));
  module.directive('smartFloat',              require('./directives/smartFloatDirective'));
  module.directive('str2integer',             require('./directives/str2integerDirective'));
  module.directive('truncateToggle',          require('./directives/truncateToggleDirective'));
  module.directive('updateRemoveButtons',     require('./directives/updateRemoveButtonsDirective'));
  module.directive('pageSelect',              require('./directives/pageSelectDirective'));
  module.directive('pagedItemsList',          require('./directives/pagedItemsList/pagedItemsListDirective'));

  module.directive('positiveFloat',           require('./directives/positiveFloat/positiveFloatDirective'));
  module.directive('naturalNumber',           require('./directives/naturalNumber/naturalNumberDirective'));

  module.filter('localTime',                  require('./filters/localTimeFilter'));
  module.filter('nl2br',                      require('./filters/nl2brFilter'));
  module.filter('timeago',                    require('./filters/timeagoFilter'));
  module.filter('truncate',                   require('./filters/truncateFilter'));

  module.service('annotationUpdate',          require('./services/annotationUpdateService'));
  module.service('Panel',                     require('./services/Panel'));
  module.service('biobankApi',                require('./services/biobankApiService'));
  module.service('funutils',                  require('./services/funutils'));
  module.service('modalService',              require('./services/modalService'));
  module.service('notificationsService',      require('./services/notificationsService'));
  module.service('stateHelper',               require('./services/stateHelperService'));
  module.service('timeService',               require('./services/timeService'));
  module.service('validationService',         require('./services/validationService'));

  return {
    name: name,
    module: module
  };
});
