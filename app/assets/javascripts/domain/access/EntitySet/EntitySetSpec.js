/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('EntitySet', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              'EntitySet',
                              'Factory',
                              'TestUtils');

      this.TestUtils.addCustomMatchers();

      this.jsonObj = () => ({
        allEntities: false,
        entityData: [{
          id: this.Factory.stringNext(),
          name: this.Factory.stringNext()
        }]
      });

      this.createEntitySetFrom = (obj) => new this.EntitySet(obj);

      this.createEntitySet = () => new this.EntitySet(this.jsonObj());

    });
  });

  it('constructor with no parameters has default values', function() {
    var entitySet = new this.EntitySet();
    expect(entitySet.allEntities).toBeFalse();
    expect(entitySet.entityData).toBeEmptyArray();
  });

  describe('for creating', function() {

    it('can create from JSON', function() {
      var json = this.jsonObj(),
          entitySet = this.EntitySet.create(json);
      expect(entitySet.allEntities).toBe(json.allEntities);
      expect(entitySet.entityData).toContainAll(json.entityData);
    });

    it('fails when required fields are missing', function() {
      var self = this,
          json = this.jsonObj();
      _.keys(json).forEach(function (key) {
        var badJson = _.omit(json, [key]);
        expect(function () {
          self.EntitySet.create(badJson);
        }).toThrowError(/:Missing required property:/);
      });
    });

    it('fails when required fields on sub objects are missing', function() {
      var self = this,
          json = this.jsonObj();
      _.keys(json.entityData[0]).forEach(function (key) {
        var badJson = _.clone(json);
        badJson.entityData = json.entityData.map(function (info) {
          return _.omit(info, [ key ]);
        });
        expect(function () {
          self.EntitySet.create(badJson);
        }).toThrowError(/:Missing required property:/);
      });
    });

  });

  describe('for creating asynchronously', function() {

    it('can create from JSON', function() {
      var json = this.jsonObj();
      this.EntitySet.asyncCreate(json)
        .then((entitySet) => {
          expect(entitySet.allEntities).toBe(json.allEntities);
          expect(entitySet.entityData).toContainAll(json.entityData);
        })
        .catch(() => {
          fail('should not invoked');
        });
      this.$rootScope.$digest();
    });

    it('fails when required fields are missing', function() {
      var self = this,
          json = this.jsonObj();
      _.keys(json).forEach(function (key) {
        var badJson = _.omit(json, [key]);
        self.EntitySet.asyncCreate(badJson)
          .then(function () {
            fail('should not invoked');
          })
          .catch(function (err) {
            expect(err.message).toContain(':Missing required property:');
          });
      });
      this.$rootScope.$digest();
    });

  });

  describe('isForAllEntities', function() {

    it('valid result when allEntities set to true', function() {
      var json = { allEntities: true, entityData: [] },
          entitySet = this.createEntitySetFrom(json);
      expect(entitySet.isForAllEntities()).toBe(true);
    });

    it('valid result when allEntities set to false', function() {
      var json = { allEntities: false, entityData: [] },
          entitySet = this.createEntitySetFrom(json);
      expect(entitySet.isForAllEntities()).toBe(false);
    });

  });

  describe('isMemberOf', function() {

    it('returns true for a name in the set', function() {
      var id        = this.Factory.stringNext(),
          name      = this.Factory.stringNext(),
          entitySet = this.EntitySet.create({ allEntities: false, entityData: [{ id: id, name: name}]});
      expect(entitySet.allEntities).toBeFalse();
      expect(entitySet.isMemberOf(name)).toBeTrue();
    });

    it('returns true if set is for all entities', function() {
      var entitySet = this.EntitySet.create({ allEntities: true, entityData: [] });
      expect(entitySet.allEntities).toBeTrue();
      expect(entitySet.isMemberOf(this.Factory.stringNext())).toBeTrue();
    });

    it('returns false for a name not in the set', function() {
      var id        = this.Factory.stringNext(),
          name      = this.Factory.stringNext(),
          entitySet = this.EntitySet.create({ allEntities: false, entityData: [{ id: id, name: name}]});
      expect(entitySet.allEntities).toBeFalse();
      expect(entitySet.isMemberOf(this.Factory.stringNext())).toBeFalse();
    });

  });

  it('addEntity adds an entity', function() {
    var id        = this.Factory.stringNext(),
        name      = this.Factory.stringNext(),
        entitySet = new this.EntitySet();
    expect(entitySet.allEntities).toBeFalse();
    expect(entitySet.entityData).toBeEmptyArray();
    entitySet.addEntity(id, name);
    expect(entitySet.entityData).toBeNonEmptyArray();
    expect(entitySet.entityData).toContain({ id: id, name: name});
  });

  it('removeEntity removes an entity by name', function() {
    var id        = this.Factory.stringNext(),
        name      = this.Factory.stringNext(),
        entitySet = this.EntitySet.create({ allEntities: false, entityData: [{ id: id, name: name}]});
    expect(entitySet.allEntities).toBeFalse();
    expect(entitySet.entityData).toBeNonEmptyArray();
    entitySet.removeEntity(name);
    expect(entitySet.entityData).toBeEmptyArray();
  });

});
