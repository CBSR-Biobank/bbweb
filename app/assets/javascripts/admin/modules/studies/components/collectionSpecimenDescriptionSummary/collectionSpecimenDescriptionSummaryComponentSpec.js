/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('collectionSpecimenDescriptionSummaryDirective', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDescription',
                              'Factory');

      this.specimenDescription =
        new this.CollectionSpecimenDescription(this.Factory.collectionSpecimenDescription());

      this.createController = (specimenDescription) => {
        specimenDescription = specimenDescription || this.specimenDescription;
        ComponentTestSuiteMixin.createController.call(
          this,
          [
            '<collection-specimen-description-summary',
            '  specimen-description="vm.specimenDescription">',
            '</collection-specimen-description-summary>'
          ].join(''),
          { specimenDescription: specimenDescription },
          'collectionSpecimenDescriptionSummary'
        );
      };
    });
  });

  it('can be created', function() {
    this.createController();
    expect(this.scope.vm.specimenDescription).toBe(this.specimenDescription);
  });


});
