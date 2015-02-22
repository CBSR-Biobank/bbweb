/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 */
define([
  'angular',
  '../../../test/assets/javascripts/test/fakeDomainEntities',
  '../../../test/assets/javascripts/test/extendDomainEntities'
], function(angular) {
  'use strict';

  return angular.module('biobank.test', [
    'biobank.test.fakeDomainEntities',
    'biobank.test.extendDomainEntities'
  ]);
});
