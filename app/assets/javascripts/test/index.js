/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import ComponentTestSuiteMixin         from './ComponentTestSuiteMixin';
import DirectiveTestSuiteMixin         from './DirectiveTestSuiteMixin';
import Factory                         from './Factory';
import ModalTestSuiteMixin             from './ModalTestSuiteMixin';
import ShippingComponentTestSuiteMixin from './ShippingComponentTestSuiteMixin';
import TestSuiteMixin                  from './TestSuiteMixin';
import angular                         from 'angular';

const TestModule = angular.module('biobank.test', [])
      .service('Factory',                         Factory)
      .service('testUtils',
               require('../../../assets/javascripts/test/testUtils'))
      .service('TestSuiteMixin',                  TestSuiteMixin)
      .service('ComponentTestSuiteMixin',         ComponentTestSuiteMixin)
      .service('DirectiveTestSuiteMixin',         DirectiveTestSuiteMixin)
      .service('ModalTestSuiteMixin',             ModalTestSuiteMixin)
      .service('ShippingComponentTestSuiteMixin', ShippingComponentTestSuiteMixin)

      .factory('EntityTestSuite',
               require('../../../assets/javascripts/test/EntityTestSuite'))
      .factory('ServerReplyMixin',
               require('../../../assets/javascripts/test/ServerReplyMixin'))
      .factory('AnnotationsEntityTestSuiteMixin',
               require('../../../assets/javascripts/test/AnnotationsEntityTestSuiteMixin'))
      .factory('MebershipSpecCommon',
               require('../../../assets/javascripts/test/MembershipSpecCommon'))
      .name;

export default TestModule;
