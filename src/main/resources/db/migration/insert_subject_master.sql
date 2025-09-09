INSERT INTO subject_category (code, name, sort_order) VALUES
                                                          ('BIO','생명약학',1),
                                                          ('IND','산업약학',2),
                                                          ('CLN','임상·실무약학',3),
                                                          ('MGMT','임상·실무약무',4),
                                                          ('LAW','보건·의약관계법규',5)
    AS new
ON DUPLICATE KEY UPDATE name=new.name, sort_order=new.sort_order;


INSERT INTO subject (code, name, category_id, active) VALUES
                                                          ('BIO-BIOCHEM','생화학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-MOLBIO','분자생물학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-MICRO','미생물학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-IMMUNO','면역학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-PHARM','약물학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-PREVENT','예방약학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('BIO-PATHO','병태생리학',(SELECT id FROM subject_category WHERE code='BIO'),1),
                                                          ('IND-PHYS','물리약학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-SYN','합성학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-MEDCHEM','약화학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-ANAL','의약품분석학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-PHARM','약제학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-PHARMACOG','생약학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('IND-HERBFORM','한약제제학',(SELECT id FROM subject_category WHERE code='IND'),1),
                                                          ('CLN-PHARMACOTHER','약물치료학',(SELECT id FROM subject_category WHERE code='CLN'),1),
                                                          ('MGMT-COMMUNITY','약국실무',(SELECT id FROM subject_category WHERE code='MGMT'),1),
                                                          ('MGMT-GMP','의약품 제조 / 품질관리',(SELECT id FROM subject_category WHERE code='MGMT'),1),
                                                          ('MGMT-ADMIN','약무행정 경영관리(사회약학)',(SELECT id FROM subject_category WHERE code='MGMT'),1),
                                                          ('LAW-PHARM','약사법',(SELECT id FROM subject_category WHERE code='LAW'),1)
    AS new
ON DUPLICATE KEY UPDATE name=new.name, active=new.active;