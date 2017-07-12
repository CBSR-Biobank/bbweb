/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('centreLocationsModalService', function() {

    function SuiteMixinFactory(ModalTestSuiteMixin) {

      function SuiteMixin() {
        ModalTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ModalTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.openModal = function (heading, label, placeholder, value, locationInfosToOmit) {
        heading             = heading     || this.factory.stringNext();
        label               = label       || this.factory.stringNext();
        placeholder         = placeholder || this.factory.stringNext();
        value               = value       || this.factory.stringNext();
        locationInfosToOmit = locationInfosToOmit || [];

        this.modal = this.centreLocationsModalService.open(heading,
                                                           label,
                                                           placeholder,
                                                           value,
                                                           locationInfosToOmit);

        this.modal.result.then(function () {}, function () {});
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();
      };

      SuiteMixin.prototype.centreAndLocations = function () {
        var self = this;
        return _.map(_.range(3), function () {
          var location = self.factory.location(),
              centre = self.factory.centre({ locations: [ location ] });
          return {
            centre: centre,
            locationInfo: self.factory.centreLocationInfo(centre)
          };
        });
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, new SuiteMixinFactory(ModalTestSuiteMixin).prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'centreLocationsModalService',
                              'Centre',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/services/centreLocationsModal/centreLocationsModal.html');
      this.addModalMatchers();
      testUtils.addCustomMatchers();
    }));

    it('can open modal', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('ok button can be pressed', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.okPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('cancel button can be pressed', function() {
      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.closePressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('can display location infos', function() {
      var locationInfos = _.map(this.centreAndLocations(), function (o) {
        return o.locationInfo;
      });

      this.Centre.locationsSearch = jasmine.createSpy('centreAndLocations')
        .and.returnValue(this.$q.when(locationInfos));

      this.openModal();
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.getCentreLocationInfo().then(function (reply) {
        expect(reply).toContainAll(locationInfos);
      });
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('can omit a location info', function() {
      var locationInfos = _.map(this.centreAndLocations(), function (o) {
        return o.locationInfo;
      }),
          locationInfoToOmit = locationInfos[0],
          locationInfosToDisplay = locationInfos.slice(1);

      this.Centre.locationsSearch = jasmine.createSpy('centreAndLocations')
        .and.returnValue(this.$q.when(locationInfos));

      this.openModal(this.heading,
                     this.label,
                     this.placeholder,
                     undefined,
                     locationInfoToOmit);
      expect(this.$document).toHaveModalsOpen(1);
      this.scope.vm.getCentreLocationInfo().then(function (reply) {
        expect(reply).toContainAll(locationInfosToDisplay);
      });
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

});
