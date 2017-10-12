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
import EntityTestSuite                 from './EntityTestSuite';
import Factory                         from './Factory';
import ModalTestSuiteMixin             from './ModalTestSuiteMixin';
import ServerReplyMixin                from '../../../assets/javascripts/test/ServerReplyMixin';
import ShippingComponentTestSuiteMixin from './ShippingComponentTestSuiteMixin';
import TestSuiteMixin                  from './TestSuiteMixin';
import TestUtils                       from './TestUtils';
import angular                         from 'angular';

const TestModule = angular.module('biobank.test', [])
      .service('Factory',                         Factory)
      .service('TestUtils',                       TestUtils)
      .service('TestSuiteMixin',                  TestSuiteMixin)
      .service('ComponentTestSuiteMixin',         ComponentTestSuiteMixin)
      .service('DirectiveTestSuiteMixin',         DirectiveTestSuiteMixin)
      .service('ModalTestSuiteMixin',             ModalTestSuiteMixin)
      .service('ShippingComponentTestSuiteMixin', ShippingComponentTestSuiteMixin)
      .service('EntityTestSuite',                 EntityTestSuite)
      .service('ServerReplyMixin',                ServerReplyMixin)

      .factory('AnnotationsEntityTestSuiteMixin',
               require('../../../assets/javascripts/test/AnnotationsEntityTestSuiteMixin'))
      .factory('MebershipSpecCommon',
               require('../../../assets/javascripts/test/MembershipSpecCommon'))
      .name;

export default TestModule;
