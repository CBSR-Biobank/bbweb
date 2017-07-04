/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('collectionSpecimenDescriptionSummaryDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (specimenDescription) {
        specimenDescription = specimenDescription || this.specimenDescription;
        ComponentTestSuiteMixin.prototype.createController.call(
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

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDescription',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummary.html');

      this.specimenDescription =
        new this.CollectionSpecimenDescription(this.factory.collectionSpecimenDescription());
    }));

    it('can be created', function() {
      this.createController();
      expect(this.scope.vm.specimenDescription).toBe(this.specimenDescription);
    });


  });

});
