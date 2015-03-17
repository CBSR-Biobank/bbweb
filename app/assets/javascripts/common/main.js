/**
 * Common functionality.
 */
define(function (require) {
  'use strict';

  var angular                          = require('angular'),

      annotationsInputDirective        = require('./directives/annotationsInputDirective'),
      domainEntityTimestampsDirective  = require('./directives/domainEntityTimestampsDirective'),
      focusMeDirective                 = require('./directives/focusMeDirective'),
      infoUpdateRemoveButtonsDirective = require('./directives/infoUpdateRemoveButtonsDirective'),
      integerDirective                 = require('./directives/integerDirective'),
      panelButtonsDirective            = require('./directives/panelButtonsDirective'),
      smartFloatDirective              = require('./directives/smartFloatDirective'),
      str2integerDirective             = require('./directives/str2integerDirective'),
      truncateToggleDirective          = require('./directives/truncateToggleDirective'),
      uiBreadcrumbsDirective           = require('./directives/uiBreadcrumbsDirective'),
      updateRemoveButtonsDirective     = require('./directives/updateRemoveButtonsDirective'),

      timeagoFilter                    = require('./filters/timeagoFilter'),
      truncateFilter                   = require('./filters/truncateFilter'),

      Panel                            = require('./services/Panel'),
      biobankApiService                = require('./services/biobankApiService'),
      domainEntityRemoveService        = require('./services/domainEntityRemoveService'),
      domainEntityService              = require('./services/domainEntityService'),
      domainEntityUpdateErrorService   = require('./services/domainEntityUpdateErrorService'),
      funutils                         = require('./services/funutils'),
      modalService                     = require('./services/modalService'),
      notificationsService             = require('./services/notificationsService'),
      panelTableService                = require('./services/panelTableService'),
      queryStringService               = require('./services/queryStringService'),
      stateHelperService               = require('./services/stateHelperService'),
      tableService                     = require('./services/tableService'),
      validationService                = require('./services/validationService');

  var module = angular.module('biobank.common', ['ui.bootstrap', 'ngTable']);

  module.directive('annotationsInput', annotationsInputDirective.directive);
  module.controller('AnnotationsInputCtrl', annotationsInputDirective.controller);

  module.directive('domainEntityTimestamps', domainEntityTimestampsDirective);
  module.directive('focusMe', focusMeDirective);
  module.directive('infoUpdateRemoveButtons', infoUpdateRemoveButtonsDirective);
  module.directive('integer', integerDirective);

  module.directive('panelButtons', panelButtonsDirective.directive);
  module.controller('PanelButtonsController', panelButtonsDirective.controller);

  module.directive('smartFloat', smartFloatDirective);
  module.directive('str2integer', str2integerDirective);
  module.directive('truncateToggle', truncateToggleDirective);
  module.directive('uiBreadcrumbs', uiBreadcrumbsDirective);
  module.directive('updateRemoveButtons', updateRemoveButtonsDirective);

  module.filter('timeago', timeagoFilter);
  module.filter('truncate', truncateFilter);

  module.service('Panel', Panel);
  module.service('biobankApi', biobankApiService);
  module.service('domainEntityRemoveService', domainEntityRemoveService);
  module.service('domainEntityService', domainEntityService);
  module.service('domainEntityUpdateError', domainEntityUpdateErrorService);
  module.service('funutils', funutils);
  module.service('modalService', modalService);
  module.service('notificationsService', notificationsService);
  module.service('panelTableService', panelTableService);
  module.service('queryStringService', queryStringService);
  module.service('stateHelper', stateHelperService);
  module.service('tableService', tableService);
  module.service('validationService', validationService);

  return module;
});
