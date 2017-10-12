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
    angular.mock.inject(function(ModalTestSuiteMixin, TestUtils) {
      _.extend(this, ModalTestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$animate',
                              '$document',
                              'centreLocationsModalService',
                              'Centre',
                              'Factory');
      this.addModalMatchers();
      TestUtils.addCustomMatchers();
      this.openModal = (heading, label, placeholder, value, locationInfosToOmit) => {
        heading             = heading     || this.Factory.stringNext();
        label               = label       || this.Factory.stringNext();
        placeholder         = placeholder || this.Factory.stringNext();
        value               = value       || this.Factory.stringNext();
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
          var location = self.Factory.location(),
              centre = self.Factory.centre({ locations: [ location ] });
          return {
            centre: centre,
            locationInfo: self.Factory.centreLocationInfo(centre)
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
