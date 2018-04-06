/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

const ngModule = 'FIXME'

xdescribe('SpcLinkTypeViewer', function() {

  var SpcLinkTypeViewer, SpecimenLinkType, Factory;

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(_SpcLinkTypeViewer_,
                                 _SpecimenLinkType_,
                                 _Factory_) {
      SpcLinkTypeViewer = _SpcLinkTypeViewer_;
      SpecimenLinkType  = _SpecimenLinkType_;
      Factory           = _Factory_;
    });
  });

  function createEntities() {
    var study,
        processingType,
        specimenGroups,
        annotationTypes,
        baseSpcLinkType,
        slt;

    study = Factory.study();
    processingType = Factory.processingType(study);
    specimenGroups = [
      Factory.specimenGroup(study),
      Factory.specimenGroup(study),
    ];
    annotationTypes = [
      Factory.studyAnnotationType(study),
      Factory.studyAnnotationType(study)
    ];
    baseSpcLinkType = Factory.specimenLinkType(processingType, {
      inputGroup: specimenGroups[0],
      outputGroup: specimenGroups[1],
      annotationTypes: annotationTypes
    });
    slt = new SpecimenLinkType(baseSpcLinkType, {
      studySpecimenGroups: specimenGroups,
      studyAnnotationTypes: annotationTypes
    });

    return {
      study:           study,
      processingType:  processingType,
      annotationTypes: annotationTypes,
      baseSpcLinkType: baseSpcLinkType,
      slt:             slt
    };
  }

  it('should open a modal when created', function () {
    var modal = this.$injector.get('$uibModal'),
        entities = createEntities(),
        viewer;                                 // eslint-disable-line no-unused-vars

    spyOn(modal, 'open').and.callFake(function () {
      return this.fakeModal();
    });

    // jshint unused:false
    viewer = new SpcLinkTypeViewer(entities.slt, entities.processingType);
    expect(modal.open).toHaveBeenCalled();
  });

  it('should display valid attributes', function() {
    var EntityViewer = this.$injector.get('EntityViewer'),
        entities = createEntities(),
        attributes,
        viewer;                                 // eslint-disable-line no-unused-vars

    spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
      attributes.push({label: label, value: value});
    });

    attributes = [];
    viewer = new SpcLinkTypeViewer(entities.slt, entities.processingType);

    expect(attributes).toBeArrayOfSize(10);

    attributes.forEach((attr) => {
      switch (attr.label) {
      case 'Processing Type':
        expect(attr.value).toBe(entities.processingType.name);
        break;
      case 'Input Group':
        expect(attr.value).toBe(entities.slt.inputGroup.name);
        break;
      case 'Expected input change':
        expect(attr.value).toBe(entities.slt.expectedInputChange + ' ' + entities.slt.inputGroup.units);
        break;
      case 'Input count':
        expect(attr.value).toBe(entities.slt.inputCount);
        break;
      case 'Input Container Type':
        expect(attr.value).toBe('None');
        break;
      case 'Output Group':
        expect(attr.value).toBe(entities.slt.outputGroup.name);
        break;
      case 'Expected output change':
        expect(attr.value).toBe(entities.slt.expectedInputChange + ' ' + entities.slt.outputGroup.units);
        break;
      case 'Output count':
        expect(attr.value).toBe(entities.slt.outputCount);
        break;
      case 'Output Container Type':
        expect(attr.value).toBe('None');
        break;
      case 'Annotation Types':
        expect(attr.value).toBe(entities.slt.getAnnotationTypeDataAsString());
        break;
      default:
        jasmine.getEnv().fail('label is invalid: ' + attr.label);
      }
    });
  });

});
