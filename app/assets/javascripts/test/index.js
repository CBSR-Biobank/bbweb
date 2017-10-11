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
import ModalTestSuiteMixin             from '../../../assets/javascripts/test/ModalTestSuiteMixin';
import ShippingComponentTestSuiteMixin from '../../../assets/javascripts/test/ShippingComponentTestSuiteMixin';
import TestSuiteMixin                  from './TestSuiteMixin';
import angular                         from 'angular';

const TestModule = angular.module('biobank.test', [])
      .service('testDomainEntities',
               require('../../../assets/javascripts/test/testDomainEntities'))
      .service('factory',
               require('../../../assets/javascripts/test/factory'))
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
