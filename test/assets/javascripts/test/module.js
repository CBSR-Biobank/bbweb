/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 */
define([
  'angular',
  '../../../test/assets/javascripts/test/fakeDomainEntities',
  '../../../test/assets/javascripts/test/extendedDomainEntities'
], function(angular, fakeDomainEntities, extendedDomainEntities) {
  'use strict';

  var module = angular.module('biobank.test', []);

  module.service('fakeDomainEntities', fakeDomainEntities);
  module.service('extendedDomainEntities', extendedDomainEntities);

  return module;
});
