/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import AnnotationsEntityTestSuiteMixin  from './AnnotationsEntityTestSuiteMixin';
import ComponentTestSuiteMixin          from './ComponentTestSuiteMixin';
import DirectiveTestSuiteMixin          from './DirectiveTestSuiteMixin';
import EntityTestSuiteMixin             from './EntityTestSuiteMixin';
import Factory                          from './Factory';
import MebershipSpecCommon              from './MembershipSpecCommon';
import ModalTestSuiteMixin              from './ModalTestSuiteMixin';
import ServerReplyMixin                 from '../../../assets/javascripts/test/ServerReplyMixin';
import ShippingComponentTestSuiteMixin  from './ShippingComponentTestSuiteMixin';
import TestSuiteMixin                   from './TestSuiteMixin';
import TestUtils                        from './TestUtils';
import angular                          from 'angular';

const TestModule = angular.module('biobank.test', [])
      .service('Factory',                         Factory)
      .service('TestUtils',                       TestUtils)
      .service('TestSuiteMixin',                  TestSuiteMixin)
      .service('AnnotationsEntityTestSuiteMixin', AnnotationsEntityTestSuiteMixin)
      .service('ComponentTestSuiteMixin',         ComponentTestSuiteMixin)
      .service('DirectiveTestSuiteMixin',         DirectiveTestSuiteMixin)
      .service('ModalTestSuiteMixin',             ModalTestSuiteMixin)
      .service('ShippingComponentTestSuiteMixin', ShippingComponentTestSuiteMixin)
      .service('EntityTestSuiteMixin',            EntityTestSuiteMixin)
      .service('ServerReplyMixin',                ServerReplyMixin)
      .service('MebershipSpecCommon',             MebershipSpecCommon)
      .name;

export default TestModule;
