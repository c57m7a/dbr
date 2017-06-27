INSERT INTO array_ref (length, array_ref_id) VALUES
  (0, 505), (4, 510), (4, 779);

INSERT INTO array_ref__value (array_ref_id, value_id) VALUES
  (510, 512), (510, 513), (510, 514), (510, 515), (779, 782), (779, 783), (779, 784), (779, 785);

INSERT INTO class_loader_ref (class_loader_ref_id) VALUES
  (6);

INSERT INTO array_type (array_type_id, component_type_id) VALUES
  (506, 463), (511, 473), (780, 781);

INSERT INTO class_type (is_enum, class_type_id, superclass_class_type_id) VALUES
  (FALSE, 4, NULL), (FALSE, 8, 9), (FALSE, 11, 4), (FALSE, 12, 4), (FALSE, 13, 12), (FALSE, 14, 12),
  (FALSE, 15, 12), (FALSE, 16, 15), (FALSE, 17, 15), (FALSE, 18, 15), (FALSE, 19, 18), (FALSE, 20, 12),
  (FALSE, 21, 4), (FALSE, 23, 4), (FALSE, 26, 21), (FALSE, 27, 26), (FALSE, 25, 26), (FALSE, 28, 4),
  (FALSE, 30, 4), (FALSE, 32, 4), (FALSE, 34, 4), (FALSE, 35, 4);

INSERT INTO exception_event (exception_event_id, time, catch_location_id, exception_obj_ref_id, thread_id) VALUES
  (1, '2017-06-16 09:51:42.449000', 2, 451, 452),
  (1175, '2017-06-16 09:51:42.511000', 1176, 451, 452),
  (1177, '2017-06-16 09:51:42.827000', 1178, 1181, 452),
  (1182, '2017-06-16 09:51:42.827000', 1183, 1185, 452),
  (1186, '2017-06-16 09:51:42.843000', 1178, 1181, 452),
  (1187, '2017-06-16 09:51:42.843000', 1188, 1190, 452),
  (1191, '2017-06-16 09:51:42.843000', 1178, 1181, 452),
  (1192, '2017-06-16 09:51:43.546000', 2, 1193, 452),
  (1194, '2017-06-16 09:51:43.546000', 1176, 1193, 452),
  (1195, '2017-06-16 09:51:43.561000', 2, 1196, 452),
  (1197, '2017-06-16 09:51:43.561000', 1176, 1196, 452),
  (1444, '2017-06-16 09:51:44.043000', 1445, 1448, 452),
  (1454, '2017-06-16 09:51:44.058000', 2, 1455, 452),
  (1456, '2017-06-16 09:51:44.058000', 1176, 1455, 452),
  (1457, '2017-06-16 09:51:44.090000', 2, 1458, 452),
  (1459, '2017-06-16 09:51:44.090000', 1176, 1458, 452),
  (1472, '2017-06-16 09:51:44.152000', 2, 1473, 452),
  (1474, '2017-06-16 09:51:44.152000', 1176, 1473, 452),
  (1475, '2017-06-16 09:51:44.168000', 2, 1476, 452);

INSERT INTO field (
  field_id, is_enum_constant, is_final, is_package_private, is_private, is_protected, is_public, is_static,
  is_synthetic, is_transient, is_volatile, name, declaring_reference_type_id, type_id
) VALUES
  (1199, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'daemon', NULL, 475),
  (1204, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'stillborn', NULL, 475),
  (1207, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'threadLocals', NULL, 1208),
  (1211, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'inheritableThreadLocals', NULL, 1208),
  (1214, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 'threadStatus', NULL, 473),
  (1218, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'blockerLock', NULL, 769),
  (1222, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 'name', NULL, 463),
  (1227, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'group', NULL, 1228),
  (1239, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'priority', NULL, 473),
  (1246, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'contextClassLoader', NULL, 1247),
  (1255, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'inheritedAccessControlContext', NULL, 1256),
  (1260, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'target', NULL, 1261),
  (1272, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'stackSize', NULL, 1273),
  (1278, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, 'threadSeqNumber', NULL, 1273),
  (1286, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'tid', NULL, 1273),
  (1289, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, 'val$portNumber', NULL, 473),
  (1292, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, 'val$helperLibLoaded', NULL, 475);

INSERT INTO field_event (event_type, field_event_id, time, field_id, method_inv_id, location_id, object_id, thread_id, value_id, new_value_id)
VALUES
  ('r', 2998, '2017-06-16 09:51:45.794000', 1533, NULL, 2448, 676, 452, 926, NULL),
  ('r', 2999, '2017-06-16 09:51:45.794000', 1533, NULL, 2448, 676, 452, 926, NULL),
  ('r', 3000, '2017-06-16 09:51:45.810000', 1533, NULL, 2448, 676, 452, 926, NULL),
  ('r', 3001, '2017-06-16 09:51:45.810000', 1615, NULL, 2533, 949, 452, 680, NULL),
  ('r', 3002, '2017-06-16 09:51:45.810000', 1528, NULL, 2422, 689, 452, 685, NULL),
  ('r', 3003, '2017-06-16 09:51:45.810000', 1528, NULL, 2422, 689, 452, 685, NULL),
  ('r', 3004, '2017-06-16 09:51:45.810000', 1528, NULL, 2422, 689, 452, 685, NULL),
  ('r', 3005, '2017-06-16 09:51:45.810000', 1528, NULL, 2422, 689, 452, 685, NULL),
  ('r', 3006, '2017-06-16 09:51:45.810000', 1528, NULL, 2422, 689, 452, 685, NULL);

INSERT INTO integral_value (value, integral_value_id) VALUES
  (1, 482), (53429, 491), (1, 512), (2, 513), (3, 514), (4, 515);

INSERT INTO interface__implementor (interface_id, implementor_class_type_id) VALUES
  (10, 8), (10, 11), (10, 12), (10, 21), (10, 25), (10, 54);

INSERT INTO interface__superinterface (interface_id, superinterface_id) VALUES
  (10, 214), (79, 82), (133, 187), (136, 133), (143, 133), (155, 156);

INSERT INTO interface_type (interface_type_id) VALUES
  (10), (22), (24), (29), (31), (33), (53), (62), (68), (79);

INSERT INTO local_variable (local_variable_id, is_argument, name, declaring_method_id, type_id) VALUES
  (462, TRUE, 'args', 460, 463), (464, TRUE, 'i', 460, 465), (472, FALSE, 'p', 471, 473),
  (474, FALSE, 'helperLibLoaded', 471, 475),
  (476, FALSE, 'portNumber', 471, 473),
  (477, FALSE, 't', 471, 478), (479, TRUE, 'args', 471, 463), (487, TRUE, 'portNumber', 486, 473),
  (488, TRUE, 'helperLibLoaded', 486, 475);

INSERT INTO location (location_id, code_index, line_number, source_path, declaring_ref_type_id) VALUES
  (2, 115, 436, 'java\lang\ClassLoader.java', 3),
  (1176, 56, 415, 'java\lang\ClassLoader.java', 3),
  (1178, 113, 499, 'sun\misc\URLClassPath.java', 408),
  (1183, 22, 864, 'sun\misc\URLClassPath.java', 213),
  (1188, 16, 534, 'sun\misc\URLClassPath.java', 408),
  (1200, 6, 157, 'java\lang\Thread.java', 269),
  (1205, 11, 160, 'java\lang\Thread.java', 269),
  (1209, 16, 182, 'java\lang\Thread.java', 269),
  (1212, 21, 188, 'java\lang\Thread.java', 269);

INSERT INTO method (method_id, generic_signature, is_abstract, is_bridge, is_constructor, is_final, is_native,
                           is_obsolete, is_package_private, is_private, is_protected, is_public, is_static,
                           is_static_initializer, is_synchronized, is_synthetic, is_varargs, name, signature,
                           declaring_reference_type_id, location_id, return_type_id
) VALUES
  (449, '(Ljava/lang/String;Z)Ljava/lang/Class<*>;', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE,
    FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'loadClass', '(Ljava/lang/String;Z)Ljava/lang/Class;', NULL, NULL, 450),
  (460, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE,
                                                                             'premain',
                                                                             '(Ljava/lang/String;Ljava/lang/instrument/Instrumentation;)V',
                                                                             NULL, NULL, 461),
  (471, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE,
                                                                             'premain', '(Ljava/lang/String;)V', NULL,
                                                                             NULL, 461),
  (481, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE,
                                                                            'loadHelper', '(Ljava/lang/String;)Z', NULL,
                                                                            NULL, 475),
  (486, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE,
                                                                            'startMonitor', '(IZ)V', NULL, NULL, 461),
  (494, NULL, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
                                                                           '<init>', '(Ljava/lang/String;IZ)V', NULL,
                                                                           NULL, 461);

INSERT INTO method_inv (method_inv_id, time, exit_time, method_id, return_value_id, this_object_id, thread_id)
VALUES
  (459, '2017-06-16 09:51:43.530000', '2017-06-16 09:51:43.902000', 460, 466, NULL, 452),
  (470, '2017-06-16 09:51:43.577000', '2017-06-16 09:51:43.902000', 471, 466, NULL, 452),
  (480, '2017-06-16 09:51:43.577000', '2017-06-16 09:51:43.593000', 481, 482, NULL, 452),
  (485, '2017-06-16 09:51:43.593000', '2017-06-16 09:51:43.902000', 486, 466, NULL, 452),
  (497, '2017-06-16 09:51:43.902000', NULL, 498, NULL, 496, 496),
  (493, '2017-06-16 09:51:43.608000', '2017-06-16 09:51:43.902000', 494, 466, 496, 452),
  (499, '2017-06-16 09:51:44.199000', NULL, 500, NULL, NULL, 452);

INSERT INTO method_inv__arg_value (method_inv_id, argument_value_id) VALUES
  (1158, 694), (1158, 716), (1159, 694), (1159, 722), (1160, 694), (1160, 728), (1161, 694);

INSERT INTO obj_ref (unique_id, obj_ref_id) VALUES
  (486, 6), (367, 451), (511, 454), (350, 455), (349, 456), (348, 457), (347, 458), (500, 453), (1, 452);

INSERT INTO reference_type (is_abstract, is_failed_to_initialize, is_final, is_initialized, is_package_private, is_prepared, is_private, is_protected, is_public, is_static, is_verified, loaded_at, source_debug_extension, source_name, reference_type_id, base_reference_type_id, class_loader_id)
VALUES
  (FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:43.640000', NULL,
   'Object.java', 4, NULL, NULL),
  (FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:42.496000', NULL,
   'SecureClassLoader.java', 9, NULL, NULL),
  (FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:42.496000', NULL,
   'URLClassLoader.java', 8, NULL, NULL),
  (TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:42.496000', NULL,
   'Closeable.java', 10, NULL, NULL),
  (FALSE, FALSE, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, '2017-06-16 09:51:43.640000', NULL,
   'FileInputStream.java', 11, NULL, NULL),
  (TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:43.640000', NULL,
   'InputStream.java', 12, NULL, NULL),
  (FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE, '2017-06-16 09:51:43.640000', NULL,
   'ByteArrayInputStream.java', 13, NULL, NULL);

INSERT INTO string_ref (value, string_ref_id) VALUES
  ('53429:C:\Users\nikita\AppData\Local\JetBrains\Toolbox\apps\IDEA-U\ch-0\171.4424.56\bin', 468),
  ('C:\Users\nikita\AppData\Local\JetBrains\Toolbox\apps\IDEA-U\ch-0\171.4424.56\bin', 484),
  ('$receiver', 516), ('init', 519), ('html', 521), ('name', 522), ('head', 534), ('tag', 539), ('title', 545);

INSERT INTO thread_event (event_type, thread_event_id, time, thread_id) VALUES
  ('s', 1302, '2017-06-16 09:51:43.902000', 496),
  ('s', 1303, '2017-06-16 09:51:43.902000', 496),
  ('s', 1449, '2017-06-16 09:51:44.058000', 452),
  ('s', 1450, '2017-06-16 09:51:44.058000', 452);

INSERT INTO thread_group_ref (name, thread_group_ref_id, parent_thread_group_ref_id) VALUES
  ('system', 454, NULL), ('main', 453, 454);

INSERT INTO thread_ref (name, thread_ref_id, thread_group_ref_id) VALUES
  ('Reference Handler', 455, 454),
  ('Finalizer', 456, 454), ('Signal Dispatcher', 457, 454),
  ('Attach Listener', 458, 454), ('main', 452, 453);

INSERT INTO type (type_id, name, signature) VALUES
  (4, 'java.lang.Object', 'Ljava/lang/Object;'),
  (9, 'java.security.SecureClassLoader', 'Ljava/security/SecureClassLoader;'),
  (8, 'java.net.URLClassLoader', 'Ljava/net/URLClassLoader;'),
  (10, 'java.io.Closeable', 'Ljava/io/Closeable;'),
  (11, 'java.io.FileInputStream$1', 'Ljava/io/FileInputStream$1;'),
  (12, 'java.io.InputStream', 'Ljava/io/InputStream;'),
  (13, 'java.io.ByteArrayInputStream', 'Ljava/io/ByteArrayInputStream;');

INSERT INTO value (value_id, type_id) VALUES
  (6, 7), (451, 106), (454, 273), (455, 272), (456, 271), (457, 269), (458, 269), (453, 273);