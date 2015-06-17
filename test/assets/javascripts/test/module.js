/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 */
define(function (require) {
  'use strict';

  var angular                = require('angular'),
      extendedDomainEntities = require('../../../test/assets/javascripts/test/extendedDomainEntities'),
      fakeDomainEntities     = require('../../../test/assets/javascripts/test/fakeDomainEntities');

  var module = angular.module('biobank.test', []);

  module.service('extendedDomainEntities', extendedDomainEntities);
  module.service('fakeDomainEntities',     fakeDomainEntities);

  return module;
});
