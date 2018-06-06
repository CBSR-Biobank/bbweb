/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('inputSpecimenProcessingSummaryDirective', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDefinition',
                              'Factory');

      this.inputSpecimenProcessing =
        new this.CollectionSpecimenDefinition(this.Factory.inputSpecimenProcessing());

      this.createController = (inputSpecimenProcessing) => {
        inputSpecimenProcessing = inputSpecimenProcessing || this.inputSpecimenProcessing;
        ComponentTestSuiteMixin.createController.call(
          this,
          `<input-specimen-processing-summary
               input="vm.inputSpecimenProcessing">
           </input-specimen-processing-summary>`,
          { inputSpecimenProcessing: inputSpecimenProcessing },
          'inputSpecimenProcessingSummary'
        );
      };
    });
  });

  it('can be created', function() {
    this.createController();
    expect(this.scope.vm.inputSpecimenProcessing).toBe(this.inputSpecimenProcessing);
  });


});
