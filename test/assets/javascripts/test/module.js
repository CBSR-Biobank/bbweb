/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular');

  var module = angular.module('biobank.test', []);

  module.service('extendedDomainEntities', require('../../../test/assets/javascripts/test/extendedDomainEntities'));
  module.service('fakeDomainEntities',     require('../../../test/assets/javascripts/test/fakeDomainEntities'));
  module.service('testUtils',              require('../../../test/assets/javascripts/test/testUtils'));

  return module;
});
