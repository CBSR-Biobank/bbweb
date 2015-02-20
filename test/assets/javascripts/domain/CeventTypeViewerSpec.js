// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('CeventTypeViewer', function() {

    var CeventTypeViewer, CollectionEventType, fakeEntities, study;

    var fakeModal = {
      result: {
        then: function(confirmCallback, cancelCallback) {
          //Store the callbacks for later when the user clicks on the OK or Cancel button of the dialog
          this.confirmCallBack = confirmCallback;
          this.cancelCallback = cancelCallback;
        }
      },
      close: function( item ) {
        //The user clicked OK on the modal dialog, call the stored confirm callback with the selected item
        this.result.confirmCallBack( item );
      },
      dismiss: function( type ) {
        //The user clicked cancel on the modal dialog, call the stored cancel callback
        this.result.cancelCallback( type );
      }
    };

    beforeEach(mocks.module('biobankApp', 'biobank.fakeDomainEntities'));

    beforeEach(inject(function(_CeventTypeViewer_,
                               _CollectionEventType_,
                               fakeDomainEntities) {
      CeventTypeViewer = _CeventTypeViewer_;
      CollectionEventType = _CollectionEventType_;
      fakeEntities         = fakeDomainEntities;

      study = fakeEntities.study();
      study.specimenGroups = _.map(_.range(2), function() {
        return fakeEntities.specimenGroup(study);
      });

      study.annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });
    }));

    function createCeventObjects(study, recurring) {
      var serverCeventType = fakeEntities.collectionEventType(
        study,
        {
          specimenGroups:  study.specimenGroups,
          annotationTypes: study.annotationTypes,
          recurring:       recurring
        });

      var ceventType = new CollectionEventType(
        study,
        serverCeventType,
        {
          studySpecimenGroups: study.specimenGroups,
          studyAnnotationTypes: study.annotationTypes
        });

      return {
        serverCeventType: serverCeventType,
        ceventType:       ceventType
      };
    }

    it('should open a modal when created', function() {
      var count = 0;
      var modal = this.$injector.get('$modal');
      spyOn(modal, 'open').and.callFake(function () { return fakeModal; });

      _.each([false, true], function(recurring) {
        // jshint unused:false
        var ceventObjs = createCeventObjects(study, recurring);
        var viewer = new CeventTypeViewer(study, ceventObjs.ceventType);

        count++;
        expect(modal.open.calls.count()).toBe(count);
      });
    });

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      _.each([false, true], function(recurring) {
        var ceventObjs, viewer;

        attributes = [];
        ceventObjs = createCeventObjects(study, recurring);
        viewer = new CeventTypeViewer(study, ceventObjs.ceventType);

        expect(attributes).toBeArrayOfSize(5);

        _.each(attributes, function(attr) {
          switch (attr.label) {
          case 'Name':
            expect(attr.value).toBe(ceventObjs.serverCeventType.name);
            break;
          case 'Recurring':
            expect(attr.value).toBe(ceventObjs.serverCeventType.recurring ? 'Yes' : 'No');
            break;
          case 'Specimen Groups (Count, Amount)':
            expect(attr.value).toBe(ceventObjs.ceventType.getSpecimenGroupsAsString());
            break;
          case 'Annotation Types':
            expect(attr.value).toBe(ceventObjs.ceventType.getAnnotationTypesAsString());
            break;
          case 'Description':
            expect(attr.value).toBe(ceventObjs.serverCeventType.description);
            break;
          default:
            jasmine.getEnv().fail('label is invalid: ' + attr.label);
          }
        });
      });
    });


  });

});
