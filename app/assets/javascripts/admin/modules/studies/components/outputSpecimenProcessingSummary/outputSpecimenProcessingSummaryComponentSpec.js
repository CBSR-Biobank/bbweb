/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('outputSpecimenProcessingSummaryDirective', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDefinition',
                              'Factory');

      this.outputSpecimenProcessing =
        new this.CollectionSpecimenDefinition(this.Factory.outputSpecimenProcessing());

      this.createController = (outputSpecimenProcessing) => {
        outputSpecimenProcessing = outputSpecimenProcessing || this.outputSpecimenProcessing;
        this.createControllerInternal(
          `<output-specimen-processing-summary
               output="vm.outputSpecimenProcessing">
           </output-specimen-processing-summary>`,
          { outputSpecimenProcessing: outputSpecimenProcessing },
          'outputSpecimenProcessingSummary'
        );
      };
    });
  });

  it('can be created', function() {
    this.createController();
    expect(this.scope.vm.outputSpecimenProcessing).toBe(this.outputSpecimenProcessing);
  });


});
