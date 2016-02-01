/**
 * Module for common functionality.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular                          = require('angular'),

      annotationsInputDirective        = require('./directives/annotationsInputDirective'),
      focusMeDirective                 = require('./directives/focusMeDirective'),
      infoUpdateRemoveButtonsDirective = require('./directives/infoUpdateRemoveButtonsDirective'),
      integerDirective                 = require('./directives/integerDirective'),
      panelButtonsDirective            = require('./directives/panelButtonsDirective'),
      smartFloatDirective              = require('./directives/smartFloatDirective'),
      str2integerDirective             = require('./directives/str2integerDirective'),
      truncateToggleDirective          = require('./directives/truncateToggleDirective'),
      updateRemoveButtonsDirective     = require('./directives/updateRemoveButtonsDirective'),
      pageSelectDirective              = require('./directives/pageSelectDirective'),

      timeagoFilter                    = require('./filters/timeagoFilter'),
      truncateFilter                   = require('./filters/truncateFilter'),

      Panel                            = require('./services/Panel'),
      biobankApiService                = require('./services/biobankApiService'),
      funutils                         = require('./services/funutils'),
      modalService                     = require('./services/modalService'),
      notificationsService             = require('./services/notificationsService'),
      queryStringService               = require('./services/queryStringService'),
      stateHelperService               = require('./services/stateHelperService'),
      validationService                = require('./services/validationService');

  var module = angular.module('biobank.common', []);

  module.directive('annotationsInput',        annotationsInputDirective.directive);
  module.controller('AnnotationsInputCtrl',   annotationsInputDirective.controller);

  module.directive('focusMe',                 focusMeDirective);
  module.directive('infoUpdateRemoveButtons', infoUpdateRemoveButtonsDirective);
  module.directive('integer',                 integerDirective);

  module.directive('panelButtons',            panelButtonsDirective.directive);
  module.controller('PanelButtonsController', panelButtonsDirective.controller);

  module.directive('smartFloat',              smartFloatDirective);
  module.directive('str2integer',             str2integerDirective);
  module.directive('truncateToggle',          truncateToggleDirective);
  module.directive('updateRemoveButtons',     updateRemoveButtonsDirective);

  module.directive('pageSelect',              pageSelectDirective);

  module.filter('timeago',                    timeagoFilter);
  module.filter('truncate',                   truncateFilter);

  module.service('Panel',                     Panel);
  module.service('biobankApi',                biobankApiService);
  module.service('funutils',                  funutils);
  module.service('modalService',              modalService);
  module.service('notificationsService',      notificationsService);
  module.service('queryStringService',        queryStringService);
  module.service('stateHelper',               stateHelperService);
  module.service('validationService',         validationService);

  return module;
});
