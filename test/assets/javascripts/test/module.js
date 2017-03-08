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

  return angular.module('biobank.test', [])
    .service('testDomainEntities',
             require('../../../test/assets/javascripts/test/testDomainEntities'))
    .service('factory',
             require('../../../test/assets/javascripts/test/factory'))
    .service('testUtils',
             require('../../../test/assets/javascripts/test/testUtils'))
    .factory('EntityTestSuiteMixin',
             require('../../../test/assets/javascripts/test/EntityTestSuiteMixin'))
    .factory('ModalTestSuiteMixin',
             require('../../../test/assets/javascripts/test/ModalTestSuiteMixin'))
    .factory('TestSuiteMixin',
             require('../../../test/assets/javascripts/test/TestSuiteMixin'))
    .factory('ServerReplyMixin',
             require('../../../test/assets/javascripts/test/ServerReplyMixin'))
    .factory('AnnotationsEntityTestSuiteMixin',
             require('../../../test/assets/javascripts/test/AnnotationsEntityTestSuiteMixin'))
    .factory('ComponentTestSuiteMixin',
             require('../../../test/assets/javascripts/test/ComponentTestSuiteMixin'))
    .factory('ShippingComponentTestSuiteMixin',
             require('../../../test/assets/javascripts/test/ShippingComponentTestSuiteMixin'));
});
