/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('collectionSpecimenDescriptionSummaryDirective', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDescription',
                              'factory');

      this.specimenDescription =
        new this.CollectionSpecimenDescription(this.factory.collectionSpecimenDescription());

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
