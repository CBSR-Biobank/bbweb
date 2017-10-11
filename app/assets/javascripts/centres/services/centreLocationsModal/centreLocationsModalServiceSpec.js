/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('centreLocationsModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', 'biobankApp', 'biobank.test');
    angular.mock.inject(function(ModalTestSuiteMixin, testUtils) {
      _.extend(this, ModalTestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'centreLocationsModalService',
                              'Centre',
                              'factory');
      this.addModalMatchers();
      testUtils.addCustomMatchers();
      this.openModal = (heading, label, placeholder, value, locationInfosToOmit) => {
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

      this.centreAndLocations = () => {
        var self = this;
        return _.range(3).map(() => {
          var location = self.factory.location(),
              centre = self.factory.centre({ locations: [ location ] });
          return {
            centre: centre,
            locationInfo: self.factory.centreLocationInfo(centre)
          };
        });
      };
    });
  });

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
    var locationInfos = _.map(this.centreAndLocations(), 'locationInfo');

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
    var locationInfos = _.map(this.centreAndLocations(), 'locationInfo'),
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
