/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('collectionSpecimenDefinitionSummaryDirective', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDefinition',
                              'Factory');

      this.specimenDefinition =
        new this.CollectionSpecimenDefinition(this.Factory.collectionSpecimenDefinition());

      this.createController = (specimenDefinition) => {
        specimenDefinition = specimenDefinition || this.specimenDefinition;
        ComponentTestSuiteMixin.createController.call(
          this,
          [
            '<collection-specimen-definition-summary',
            '  specimen-definition="vm.specimenDefinition">',
            '</collection-specimen-definition-summary>'
          ].join(''),
          { specimenDefinition: specimenDefinition },
          'collectionSpecimenDefinitionSummary'
        );
      };
    });
  });

  it('can be created', function() {
    this.createController();
    expect(this.scope.vm.specimenDefinition).toBe(this.specimenDefinition);
  });


});
