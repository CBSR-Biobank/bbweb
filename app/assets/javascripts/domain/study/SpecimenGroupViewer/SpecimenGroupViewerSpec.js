/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

xdescribe('SpecimenGroupViewer', function() {

  var SpecimenGroupViewer, Factory, centre;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(_SpecimenGroupViewer_,
                                 _Factory_) {
      SpecimenGroupViewer = _SpecimenGroupViewer_;
      Factory   = _Factory_;

      centre = Factory.centre();
    });
  });

  it('should open a modal when created', inject(function (TestUtils) {
    var modal = this.$injector.get('$uibModal'),
        study,
        specimenGroup,
        viewer;

    spyOn(modal, 'open').and.callFake(function () {
      return TestUtils.fakeModal();
    });

    // jshint unused:false
    study = Factory.study();
    specimenGroup = Factory.specimenGroup(study);
    viewer = new SpecimenGroupViewer(specimenGroup);

    expect(modal.open).toHaveBeenCalled();
  }));

  it('should display valid attributes', function() {
    var EntityViewer = this.$injector.get('EntityViewer');
    var attributes, study, specimenGroup, viewer;

    spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
      attributes.push({label: label, value: value});
    });

    attributes = [];
    study = Factory.study();
    specimenGroup = Factory.specimenGroup(study);
    viewer = new SpecimenGroupViewer(specimenGroup);

    expect(attributes).toBeArrayOfSize(7);

    attributes.forEach((attr) => {
      switch (attr.label) {
      case 'Name':
        expect(attr.value).toBe(specimenGroup.name);
        break;
      case 'Units':
        expect(attr.value).toBe(specimenGroup.units);
        break;
      case 'Anatomical Source':
        expect(attr.value).toBe(specimenGroup.anatomicalSourceType);
        break;
      case 'Preservation Type':
        expect(attr.value).toBe(specimenGroup.preservationType);
        break;
      case 'Preservation Temperature':
        expect(attr.value).toBe(specimenGroup.preservationTemperatureType);
        break;
      case 'Specimen Type':
        expect(attr.value).toBe(specimenGroup.specimenType);
        break;
      case 'Description':
        expect(attr.value).toBe(specimenGroup.description);
        break;
      default:
        jasmine.getEnv().fail('label is invalid: ' + attr.label);
      }
    });
  });

});
